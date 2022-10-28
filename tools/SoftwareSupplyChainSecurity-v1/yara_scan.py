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
                for exclude in exclude_list:
                    if exclude in _check_name:
                        print(f"Skip{one_file}. (match {exclude})   ")
                    else:
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
                        if all_file == file_type:
                            self.job_set.add((all_file, yara_path))

    def run_jobs(self):
        """
        执行扫描任务，结果存放到 self.result 里
        :return:
        """
        # 以文件名为 key，groupby，保证每个文件只需要打开一次
        for file_path, yara_group in itertools.groupby(self.job_set, lambda x: x[0]):
            with open(file_path, 'rb') as fd:
                data = fd.read()
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
            # 1. 先区分开普通匹配和核心匹配
            core_match, normal_match = [], []
            for offset, condition, match_bytes in yara_match.strings:
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
                    result['checkResult'] = f'{line_number} : {line_content}'
                    result['keyLogInfo'] = f'{line_number} : {piece}'
                    result['hash'] = md5(line_content.encode()).hexdigest()
                    self.result.append(result)
            else:
                # 3. 如果匹配结果里有 _CORE，就按照 _CORE 的个数拆分为 N 个漏洞
                for offset, condition, match_bytes in core_match:
                    result = {
                        'suspiciousFileName': file_path,
                        'ruleName': yara_path,
                    }
                    line_number, piece, line_content = self.offset_to_content(data, match_bytes, offset)
                    _with_line_number: list[tuple[int, str, str]] = list()
                    _with_line_number.append((line_number, piece, line_content))
                    for _offset, _, _match_bytes in normal_match:
                        _with_line_number.append((self.offset_to_content(data, _match_bytes, _offset)))
                    _with_line_number.sort()
                    result['checkResult'] = '\n'.join(
                        [f'{line_number} : {line_content}' for line_number, _, line_content in _with_line_number]
                    )
                    result['keyLogInfo'] = '\n'.join(
                        [f'{line_number} : {piece}' for line_number, piece, _ in _with_line_number]
                    )
                    result['hash'] = md5(line_content.encode()).hexdigest()
                    self.result.append(result)

    @staticmethod
    def offset_to_content(data, match_bytes, offset) -> (int, str, str):
        line_number = data[:offset].count(b'\n') + 1
        line_start = data.rfind(b'\n', 0, offset) + 1
        line_end = data.find(b'\n', offset) + 1
        try:
            piece = match_bytes.decode()
            line_content = data[line_start: line_end].decode()
        except UnicodeDecodeError:
            piece = "DECODE ERROR"
            line_content = "DECODE ERROR"
        return line_number, piece, line_content


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


@functools.cache
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
