import functools
import itertools
import os.path
from hashlib import md5
from typing import Optional

import yaml
import yara


class YaraScan(object):
    def __init__(self, source_dir, config_path):
        """

        :param source_dir: 待扫描文件路径
        :param config_path: 配置文件路径
        """
        self.source_dir = source_dir
        self.config_path = config_path
        with open(config_path) as fd:
            self.config = yaml.safe_load(fd)
        self.all_files: list[str] = list()
        self.job_set: set[tuple[str, str]] = set()
        self.result: list[dict] = list()

    def scan(self):
        self.generate_file_list()
        self.generate_tasks()
        self.run_jobs()

    def generate_file_list(self):
        """
        递归便利目录，排除不需要的文件，存放到 self.all_files 里
        """
        exclude_list = [x.lower() for x in self.config['exclude']]
        for root, _, files in os.walk(self.source_dir):
            all_files = [os.path.join(root, file) for file in files]
            for one_file in all_files:
                _check_name = os.path.relpath(one_file, self.source_dir).lower()
                skip = False
                for exclude in exclude_list:
                    if exclude in _check_name:
                        print(f"Skip {one_file}. (match {exclude})")
                        skip = True
                        break
                if not skip:
                    self.all_files.append(one_file)

        print("Found files:", len(self.all_files))

    def generate_tasks(self):
        """
        根据配置文件中的 scan_tasks，生成扫描任务
        :return:
        """
        for task in self.config['scan_tasks']:
            self.generate_task(task)
        print("Generate jobs:", len(self.job_set))

    def generate_task(self, task):
        """
        根据配置文件中的 rules，生成扫描任务，存放到 self.job_set 里
        :param task:
        :return:
        """
        for rule in task['rules']:
            yara_path = rule['rule']
            for file_type in rule['type']:
                if file_type == '*':
                    for all_file in self.all_files:
                        self.job_set.add((all_file, yara_path))
                elif file_type.startswith('.'):
                    for all_file in self.all_files:
                        if all_file.endswith(file_type):
                            self.job_set.add((all_file, yara_path))
                else:
                    for all_file in self.all_files:
                        if os.path.basename(all_file) == file_type:
                            self.job_set.add((all_file, yara_path))

    def run_jobs(self):
        """
        执行扫描任务，结果存放到 self.result 里
        :return:
        """
        # 以文件名为 key，groupby，保证每个文件只需要打开一次
        for file_path, yara_group in itertools.groupby(self.job_set, lambda x: x[0]):
            try:
                with open(file_path, 'rb') as fd:
                    data = fd.read()
            except FileNotFoundError:
                # 一些git仓里，软连接指向的位置是不存在的文件，会抛出该异常
                print("Cannot find", file_path)
            else:
                for yara_path in [x[1] for x in yara_group]:
                    yara_rule = _get_yara_rule(yara_path)
                    if yara_rule is None:
                        continue
                    yara_result = yara_rule.match(data=data)
                    if yara_result:
                        self.handle_matches(file_path, data, yara_result, yara_path)

    def handle_matches(self, file_path: str, data: bytes, yara_matches: yara.Match, yara_path: str):
        """
        处理扫描结果
        """
        for yara_match in yara_matches:
            # apply post handler
            post_strings = yara_match.strings
            if yara_match.rule == 'const_b64_or_hex':
                # python: 由于过长的驼峰API会触发b64的规则，而b64规则可以由其他规则覆盖，因此移除该类误报
                continue
            elif yara_match.rule == 'os_related':
                post_strings = postH_os_related(post_strings)
            elif yara_match.rule == 'connect_related':
                post_strings = postH_connect_related(post_strings)
            elif yara_match.rule == 'dangerous_command':
                post_strings = postH_dangerous_command(post_strings)
            elif yara_match.rule == 'define_password':
                post_strings = postH_define_password(post_strings)

            if not post_strings:
                continue

            # 1. 先区分开普通匹配和核心匹配
            core_match, normal_match = [], []
            for offset, condition, match_bytes in post_strings:
                if '_CORE' in condition:
                    core_match.append((offset, condition, match_bytes))
                else:
                    normal_match.append((offset, condition, match_bytes))
            if len(core_match) == 0:
                # 2. 如果匹配结果里没有 _CORE，就按照匹配结果的个数拆分为 N 个漏洞
                for offset, condition, match_bytes in normal_match:
                    result = {
                        'suspiciousFileName': file_path,
                        'ruleName': yara_path,
                    }
                    line_number, piece, line_content = self.offset_to_content(data, match_bytes, offset)
                    # 2.5 针对Java文件，忽略它的javadoc代码
                    if file_path.endswith('.java'):
                        if line_content.strip().startswith('*') or line_content.strip().startswith('//'):
                            continue
                    # 2.5 针对Python文件，忽略它的注释代码
                    if file_path.endswith('.py'):
                        if line_content.strip().startswith('#'):
                            continue
                    result['checkResult'] = f'{line_number} : {line_content}'
                    result['checkResultV2'] = self.linenum_around(data, line_number)
                    result['keyLogInfo'] = f'{line_number} : {piece}'
                    result['hash'] = md5(line_content.encode()).hexdigest()
                    # 对于同一个触发点，不需要报告多次，使用hash去重
                    if not any(x for x in self.result if
                               x['suspiciousFileName'] == result['suspiciousFileName'] and x['hash'] == result['hash']):
                        self.result.append(result)
            else:
                # 3. 如果匹配结果里有 _CORE，就按照 _CORE 的个数拆分为 N 个漏洞
                for offset, condition, match_bytes in core_match:
                    result = {
                        'suspiciousFileName': file_path,
                        'ruleName': yara_path,
                    }
                    line_number, piece, line_content = self.offset_to_content(data, match_bytes, offset)
                    # 2.5 针对Java文件，忽略它的javadoc代码
                    if file_path.endswith('.java'):
                        if line_content.strip().startswith('*') or line_content.strip().startswith('//'):
                            continue
                    # 2.5 针对Python文件，忽略它的注释代码
                    if file_path.endswith('.py'):
                        if line_content.strip().startswith('#'):
                            continue
                    _with_line_number: list[tuple[int, str, str]] = list()
                    _with_line_number.append((line_number, piece, line_content))

                    # 某些行，会别两条特征同时检测到，需要使用 line_number 对结果进行去重
                    _collected_lines: set[int] = set()
                    _collected_lines.add(line_number)
                    for _offset, _, _match_bytes in normal_match:
                        _line_number, _piece, _line_content = self.offset_to_content(data, _match_bytes, _offset)
                        if _line_number not in _collected_lines:
                            _with_line_number.append((_line_number, _piece, _line_content))
                            _collected_lines.add(_line_number)
                    _with_line_number.sort()


                    result['checkResult'] = '\n'.join(
                        [f'{line_number} : {line_content}' for line_number, _, line_content in _with_line_number]
                    )
                    result['checkResultV2'] = self.linenum_around(data, line_number, [_[0] for _ in _with_line_number])
                    result['keyLogInfo'] = '\n'.join(
                        [f'{line_number} : {piece}' for line_number, piece, _ in _with_line_number]
                    )
                    result['hash'] = md5(line_content.encode()).hexdigest()
                    # 对于同一个触发点，不需要报告多次，使用hash去重
                    if not any(x for x in self.result if
                               x['suspiciousFileName'] == result['suspiciousFileName'] and x['hash'] == result['hash']):
                        self.result.append(result)

    AROUND_COUNT = 3

    @staticmethod
    def linenum_around(data: bytes, hit_line_number: int, extra_line_numbers=None) -> list:
        """
        返回用于前端展示的扫描结果
        :param data: 文本内容
        :param hit_line_number: 核心命中的行号
        :param extra_line_numbers: 其余相关的行号列表
        :return: 返回list，list每个元素是dict，包含 "line", "content", "highlight" 3个字段
        """
        all_lines = ['PLACE_HOLDER']
        all_lines.extend(data.split(os.linesep.encode()))
        line_start = max(hit_line_number - YaraScan.AROUND_COUNT, 1)
        line_end = min(hit_line_number + YaraScan.AROUND_COUNT, len(all_lines) - 1)
        output_lines = list(range(line_start, line_end + 1))
        if extra_line_numbers:
            output_lines.extend(extra_line_numbers)
            output_lines = list(set(output_lines))
            output_lines.sort()
        try:
            ret = [{
                'line': i,
                'content': all_lines[i].decode(),
                'highlight': True if i == hit_line_number else False,
            } for i in output_lines]
        except UnicodeDecodeError:
            return [{'line': 1,
                     'content': "DECODE_ERROR",
                     'highlight': False,
                     }]
        else:
            return ret

    @staticmethod
    def offset_to_content(data, match_bytes, offset) -> (int, str, str):
        line_number = data[:offset].count(os.linesep.encode()) + 1
        line_start = data.rfind(os.linesep.encode(), 0, offset) + 1
        line_end = data.find(os.linesep.encode(), offset) + 1
        try:
            piece = match_bytes.decode()
            line_content = data[line_start: line_end].decode()
        except UnicodeDecodeError:
            piece = "DECODE ERROR"
            line_content = "DECODE ERROR"
        return line_number, piece, line_content


def postH_os_related(yara_strings) -> list:
    striped = list()
    for idx, (offset, condition, match_bytes) in enumerate(yara_strings):
        # 由于规则中错误地展示了 'os.*'，需要将其移除
        # 单纯导入 os 包并不能与恶意行为划等号，需要将其移除
        if condition == '$os_ini1' or condition == '$os_ini2':
            striped.append(idx)
        # 创建目录和移除文件不能视为投毒行为，目前尚未有相关案例
        elif condition == '$os_ope8' or condition == '$os_ope9' or condition == '$os_ope10':
            striped.append(idx)
    if not striped:
        return yara_strings
    return [i for j, i in enumerate(yara_strings) if j not in striped]


def postH_connect_related(yara_strings) -> list:
    # 规则中 reg1和reg3 会冲，移除reg1的报告
    striped = list()
    for idx, (offset, condition, match_bytes) in enumerate(yara_strings):
        if condition == '$ip_reg1':
            striped.append(idx)
    if not striped:
        return yara_strings
    return [i for j, i in enumerate(yara_strings) if j not in striped]

def postH_dangerous_command(yara_strings) -> list:
    striped = list()
    for idx, (offset, condition, match_bytes) in enumerate(yara_strings):
        # 修改当前进程的环境变量不会影响到整个系统，屏蔽该类误报
        if condition == '$dangerous_command11':
            striped.append(idx)
    if not striped:
        return yara_strings
    return [i for j, i in enumerate(yara_strings) if j not in striped]


def postH_define_password(yara_strings) -> list:
    striped = list()
    for idx, (offset, condition, match_bytes) in enumerate(yara_strings):
        # 宏定义的内容如果是数字和枚举，并非字符串，则不应当视为密码
        _slices = match_bytes.split()
        if len(_slices) < 3 \
                or _slices[2].isdigit() \
                or _slices[2].startswith(b'0'):  # 0x 0o 0b
            striped.append(idx)
    if not striped:
        return yara_strings
    return [i for j, i in enumerate(yara_strings) if j not in striped]


class YaraJob(object):
    def __init__(self, file_path: str, yara_path: str):
        self.file_path = file_path
        self.yara_path = yara_path

    def __eq__(self, other):
        return isinstance(other, YaraJob) \
               and self.file_path == other.file_path \
               and self.yara_path == other.yara_path

    def __hash__(self):
        return hash((self.file_path, self.yara_path))


@functools.lru_cache(maxsize=None)
def _get_yara_rule(yara_path: str) -> Optional[yara.Rules]:
    print("Load yara:", yara_path)
    # 优先加载 .cp 文件，其次加载文本规则
    if os.path.exists(yara_path + '.cp'):
        return yara.load(yara_path + '.cp')
    elif os.path.exists(yara_path):
        return yara.compile(filepath=yara_path)
    else:
        # 如果文件不存在，不报错，只打印日志，建议在变更扫描规则时，由上层调用者检测规则是否存在
        print("Cannot find yara rule", yara_path)
        return None
