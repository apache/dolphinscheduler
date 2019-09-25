# frontend-deployment

The front-end has three deployment modes: automated deployment, manual deployment and compiled source deployment.



## Preparations

#### Download the installation package

Please download the latest version of the installation package, download address： [gitee](https://gitee.com/easyscheduler/EasyScheduler/attach_files/)

After downloading escheduler-ui-x.x.x.tar.gz，decompress`tar -zxvf escheduler-ui-x.x.x.tar.gz ./`and enter the`escheduler-ui`directory




## Deployment

Automated deployment is recommended for either of the following two ways

### Automated Deployment

Edit the installation file`vi install-escheduler-ui.sh` in the` escheduler-ui` directory

Change the front-end access port and the back-end proxy interface address

```
# Configure the front-end access port
esc_proxy="8888"

# Configure proxy back-end interface
esc_proxy_port="http://192.168.xx.xx:12345"
```

>Front-end automatic deployment based on Linux system `yum` operation, before deployment, please install and update`yum`

under this directory, execute`./install-escheduler-ui.sh` 


### Manual Deployment

Install epel source `yum install epel-release -y`

Install Nginx `yum install nginx -y`


> ####  Nginx configuration file address

```
/etc/nginx/conf.d/default.conf
```

> ####  Configuration information (self-modifying)

```
server {
    listen       8888;# access port
    server_name  localhost;
    #charset koi8-r;
    #access_log  /var/log/nginx/host.access.log  main;
    location / {
        root   /xx/dist; # the dist directory address decompressed by the front end above (self-modifying)
        index  index.html index.html;
    }
    location /escheduler {
        proxy_pass http://192.168.xx.xx:12345; # interface address (self-modifying)
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header x_real_ipP $remote_addr;
        proxy_set_header remote_addr $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_http_version 1.1;
        proxy_connect_timeout 4s;
        proxy_read_timeout 30s;
        proxy_send_timeout 12s;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
    #error_page  404              /404.html;
    # redirect server error pages to the static page /50x.html
    #
    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
        root   /usr/share/nginx/html;
    }
}
```

> ####  Restart the Nginx service

```
systemctl restart nginx
```

#### nginx command

- enable `systemctl enable nginx`

- restart `systemctl restart nginx`

- status `systemctl status nginx`


## FAQ
#### Upload file size limit

Edit the configuration file `vi /etc/nginx/nginx.conf`

```
# change upload size
client_max_body_size 1024m
```


