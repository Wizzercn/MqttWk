### 证书生成教程
* keystore 文件夹下为所有密码为 123456 生成的证书文件

1、生成.key文件

```
openssl genrsa -des3 -out server.key 2048
```
中间会提示输入密码(重复输入两次)，要记住这个密码；

2、生成.crt文件
```
openssl req -new -x509 -key server.key -out server.crt -days 3650
```
会提示输入server.key的密码
开始输入Country Name：CN
State or Province Name：SH
Locality Name：shanghai
Organization Name：这个可以忽略
Organizational Unit Name：这个可以忽略
Common Name：这个可以忽略
Email Address：填写一个非QQ的邮箱地址

3、生成.pfx文件
```
openssl pkcs12 -export -out server.pfx -inkey server.key -in server.crt
```
提示输入server.key文件的密码
提示输入即将生成的.pfx文件的密码(需要输入两次)

4、生成.cer文件
```
openssl pkcs12 -nodes -nokeys -in server.pfx -passin pass:证书密码 -out  server.cer
```
如无需加密pem中私钥，可以添加选项-nodes；

如无需导出私钥，可以添加选项-nokeys；

5、生成.jks文件 
```
keytool -importkeystore -srckeystore server.pfx -destkeystore server.jks -srcstoretype PKCS12 -deststoretype JKS
```
输入证书密码，六位长度，输入原始密码；