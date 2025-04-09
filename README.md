# ezmm

## Introduction

> graalvm java脱离了jvm（也许免杀能用上？），可以直接将java代码编译成win、mac、linux平台的二进制文件，速度非常快，刚好尝试一下顺便编写一个使用hae规则配置文件的本地敏感信息收集工具

使用graalvm编译的本地文件敏感信息收集工具，依靠hae的规则配置文件

## QuickStart

可以直接用hae的配置文件Rules.yml，手动放到下面的目录下
1. Linux/Mac用户的配置文件目录：~/.config/ezmm/Rules.yml
2. Windows用户的配置文件目录：%USERPROFILE%/.config/ezmm/Rules.yml

ezmm会自动遍历当前目录下的所有文本文件，并通过hae规则文件匹配敏感信息，并生成cvs文件

![Kapture 2025-03-25 at 10 35 31](https://github.com/user-attachments/assets/ce63628b-8dd7-49be-8eeb-e7372d906edf)


## THANKS
https://github.com/gh0stkey/HaE
