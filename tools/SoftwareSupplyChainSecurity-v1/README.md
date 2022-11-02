# 环境依赖

- Linux操作系统
- python3.10（只能是3.10，因为交付时pyc是3.10的）
- java（具体版本尚未验证，运行joern需要java）
- rpm、tar、7zip、gem、unzip。如果输入是 .src.rpm，尝试对 .src.rpm 解压时会调用这些命令。
- pip依赖：`pip3 install pyyaml yara_python`
- joern依赖：`export JOERN_HOME=/path/to/joern`
  - 下载链接：https://github.com/joernio/joern/releases/download/v1.1.728/joern-cli.zip

# 输入

第一个参数： 扫描路径。如果传参是目录，则扫描该路径下的源码，如果传参是src.rpm文件，则解压后扫描其内容。

第二个参数： 输出json路径。如果传参是`stdout`，则输出到stdout。

可选参数（至少有1个）：

- enable-diff        开启diff扫描
- enable-c           开启c扫描
- enable-java        开启java扫描（依赖环境变量 JOERN_HOME）
- enable-javascript  开启javascript扫描
- enable-python      开启python扫描
- enable-ruby        开启ruby扫描

# 输出json字段说明

| field              | type | data               |
|--------------------|------|--------------------|
| ruleName           | str  | 规则名                |
| suspiciousFileName | str  | 文件名                |
| checkResult        | str  | 扫描结果，用于人类阅读（包含换行符） |
| keyLogInfo         | str  | 检测到的核心代码片段（包含换行符）  |

# example

扫描rpm包里的C语言代码：

```
python3 ./openeuler_scan.py --enable-c xxx.src.rpm report.json
```

扫描该目录下的python和java代码。虽然支持同时扫描多种语言，但仍然建议不要同时开启多个。

```
python3 ./openeuler_scan.py --enable-python --enable-java /foo/bar/all-in-one report.json
```


# help

```
python3 ./openeuler_scan.py -h
usage: openeuler_scan.py [-h] [--enable-diff] [--enable-c] [--enable-java] [--enable-javascript] [--enable-python] [--enable-ruby] source output

positional arguments:
  source               扫描路径。如果传参是目录，则扫描该路径下的源码，如果传参是src.rpm文件，则解压后扫描其内容
  output               输出json路径。如果传参是`stdout`，则输出到stdout。

options:
  -h, --help           show this help message and exit
  --enable-diff        开启diff扫描
  --enable-c           开启c扫描
  --enable-java        开启java扫描（依赖环境变量 JOERN_HOME）
  --enable-javascript  开启javascript扫描
  --enable-python      开启python扫描
  --enable-ruby        开启ruby扫描
  --custom-yaml        扫描配置，传递一个yaml文件。此条目会覆盖其他语言级规则
```
