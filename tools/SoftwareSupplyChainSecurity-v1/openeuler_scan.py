import argparse
import json
import os.path

from yara_scan import YaraScan


def main():
    parser = prepare_parser()
    args = parser.parse_args()

    if not any([args.enable_diff, args.enable_c, args.enable_java, args.enable_javascript,
                args.enable_python, args.enable_ruby, args.custom_yaml]):
        parser.print_help()
        print("请开启至少 1 个扫描规则")
        exit(-1)

    if not os.path.exists(args.source):
        print("输入文件/文件夹不存在")
        exit(-1)

    source_dir = args.source
    enabled_scanner = list()
    if args.custom_yaml:
        enabled_scanner.append(YaraScan(source_dir, args.custom_yaml))
    else:
        if args.enable_diff:
            enabled_scanner.append(YaraScan(source_dir, 'diff_scan.yaml'))
        if args.enable_c:
            enabled_scanner.append(YaraScan(source_dir, 'c_scan.yaml'))
        if args.enable_java:
            enabled_scanner.append(YaraScan(source_dir, 'java_scan.yaml'))
        if args.enable_javascript:
            enabled_scanner.append(YaraScan(source_dir, 'javascript_scan.yaml'))
        if args.enable_python:
            enabled_scanner.append(YaraScan(source_dir, 'python_scan.yaml'))
        if args.enable_ruby:
            enabled_scanner.append(YaraScan(source_dir, 'ruby_scan.yaml'))
        enabled_scanner.append(YaraScan(source_dir, 'password_scan.yaml'))

    total_result = list()
    for scanner in enabled_scanner:
        scanner.scan()
        total_result.extend(scanner.result)
        print(f"Scan {scanner.config_path}, found result {len(scanner.result)}")
    print(f"Amount, found result {len(total_result)}")

    # 如果传参是stdout，则直接打印
    if args.output == 'stdout':
        print(json.dumps(total_result, ensure_ascii=False, indent=4))
    else:
        with open(args.output, 'w', encoding='utf-8') as fd:
            json.dump(total_result, fd, ensure_ascii=False, indent=4)


def prepare_parser():
    parser = argparse.ArgumentParser()
    parser.add_argument("--enable-diff", help='开启diff扫描', action='store_true')
    parser.add_argument("--enable-c", help='开启c扫描', action='store_true')
    parser.add_argument("--enable-java", help='开启java扫描', action='store_true')
    parser.add_argument("--enable-javascript", help='开启javascript扫描', action='store_true')
    parser.add_argument("--enable-python", help='开启python扫描', action='store_true')
    parser.add_argument("--enable-ruby", help='开启ruby扫描', action='store_true')
    parser.add_argument("--custom-yaml", help="扫描配置，传递一个yaml文件。此条目会覆盖其他语言级规则")
    parser.add_argument("source", help='扫描路径，传递一个目录')
    parser.add_argument("output", help='输出json路径。如果传参是`stdout`，则输出到stdout。')
    return parser


if __name__ == '__main__':
    main()
