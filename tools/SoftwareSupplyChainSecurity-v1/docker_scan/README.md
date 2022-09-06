## 使用yara_scan扫描一个项目

扫描一个项目：

```
python3 ./start_yara_scan.py [-c CONFIG] source_dir output_dir storage_dir
```

## 扫描多个项目

如需扫描一个大目录包含所有项目文件夹，需要循环调用，会共用编译好的yara规则
扫描全体目录的wrapper脚本见`docker_scan/scan_all_repo.sh`

``` shell
./scan_all_repo.sh source_dir output_dir storage_dir [config]
```
`storage_dir`

## 配置

默认配置`docker_scan/config_all.yaml`, 使用所有规则扫描 如需自定义配置，新建`xxx.yaml`文件,
在`scanner.scan()` 前调用:
`scanner.set_config('docker_scan/config_all.yaml')`
配置格式参考`docker_scan/config_all.yaml`

```
scan_tasks:
    - name: Yara scan (c)
      rule_dir: c_scan/c_yara
      rules:
          - rule: .yar
            type: [.c, .h, .cpp, .cc, .cxx, .hpp, .c++]
          - rule: .yar_all
            type: [.c, .h, .cpp, .cc, .cxx, .hpp, .c++]
    - name: Yara scan (js)
      rule_dir: js_scan
      rules:
          - rule: .yar_all
            type: [.js, ~node, ~bash]
# 过滤文件路径, 只在项目文件夹内搜索，项目目录包括以下string不会被过滤
exclude: 
    - test
    - demo
shebang_exam: yes
```

`name`: 任务名称

`rule_dir`: 任务所需yara规则存放目录，以docker_scan为相对路径

`rules[rule]`: yara规则文件名，会在`rule_dir`中寻找，每条rule文件名需唯一, 也可以使用wildcard，以`.`
开头表示使用所有相同后缀文件

`type`: 规则所适配的文件类型，`.`开头代表后缀名，`~`开头代表shebang二进制名，否则解析为文件名

`exclude`: 文件路径过滤，只要文件对于项目路径的相对路径包含下列字符串（e.g. source项目名中的test不会被过滤），就会被过滤

`shebang_exam` 是否打开文件检查shebang, yes/no

## TODO: rpm_scan和yara_scan需要重新适配!!

## Pip Dependencies

pyyaml 6.0

yara_python 4.2.0