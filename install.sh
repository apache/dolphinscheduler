#!/bin/bash

# Basic environment variables
RAINBOND_VERSION=${VERSION:-'v5.17.3'}
IMGHUB_MIRROR=${IMGHUB_MIRROR:-'registry.cn-hangzhou.aliyuncs.com/goodrain'}

# Define colorful stdout
RED='\033[0;31m'
GREEN='\033[32;1m'
YELLOW='\033[33;1m'
NC='\033[0m'
TIME="+%Y-%m-%d %H:%M:%S"

########################################
# Information collection
# Automatically collect the install details.
# Help us improve the success rate of installation.
########################################

function send_msg() {
    dest_url="https://log.rainbond.com"
    #msg=${1:-"Terminating by userself."}
    if [ -z "$1" ]; then
        msg="Terminating by userself."
    else
        msg=$(echo $1 | tr '"' " " | tr "'" " ")
    fi
    # send a message to remote url
    curl --silent -H "Content-Type: application/json" -X POST "$dest_url/dindlog" \
        -d "{\"message\":\"$msg\", \"os_info\":\"${OS_INFO}\", \"eip\":\"$EIP\", \"uuid\":\"${UUID}\"}" 2>&1 >/dev/null || :

    if [ "$msg" == "Terminating by userself." ]; then
        exit 1
    fi
}

function send_info() {
    info=$1
    echo -e "${GREEN}$(date "$TIME") INFO: $info${NC}"
    send_msg "$info"
}

function send_warn() {
    warn=$1
    echo -e "${YELLOW}$(date "$TIME") WARN: $warn${NC}"
    send_msg "$warn"
}

function send_error() {
    error=$1
    echo -e "${RED}$(date "$TIME") ERROR: $error${NC}"
    send_msg "$error"
}

# Trap SIGINT signal when detect Ctrl + C
trap send_msg SIGINT

########################################
# OS Detect
# Automatically check the operating system type.
# Return Linux or Darwin.
########################################

OS_TYPE=$(uname -s)
if [ "${OS_TYPE}" == "Linux" ]; then
    MD5_CMD="md5sum"
    if find /lib/modules/$(uname -r) -type f -name '*.ko*' | grep iptable_raw; then
        if ! lsmod | grep iptable_raw; then
            echo iptable_raw >/etc/modules-load.d/iptable_raw.conf
            modprobe iptable_raw
        fi
    fi
elif [ "${OS_TYPE}" == "Darwin" ]; then
    MD5_CMD="md5"
else
    if [ "$LANG" == "zh_CN.UTF-8" ]; then
        send_error "${OS_TYPE} 操作系统暂不支持"
        exit 1
    else
        send_error "Rainbond do not support ${OS_TYPE} OS"
        exit 1
    fi
fi

OS_INFO=$(uname -a)
UUID=$(echo $OS_INFO | ${MD5_CMD} | cut -b 1-32)

################ Start #################
if [ "$LANG" == "zh_CN.UTF-8" ]; then
    send_info "欢迎您安装 Rainbond, 如果您安装遇到问题, 请反馈到 https://www.rainbond.com/community/support"
else
    send_info "Welcome to install Rainbond, If you install problem, please feedback to https://www.rainbond.com/community/support"
fi

########################################
# Envrionment Check
# Check docker is running or not.
# Check ports can be use or not.
# If not, quit.
########################################

if ! (docker info &>/dev/null); then
    if (which docker &>/dev/null); then
        if [ "$LANG" == "zh_CN.UTF-8" ]; then
            send_error "未检测到 Docker 守护进程, 请先启动 Docker.\n\t- Linux 系统请执行 'systemctl start docker' 启动 Docker.\n\t- MacOS 系统请先启动 Docker Desktop APP.\n\t- 然后重新执行本脚本."
            exit 1
        else
            send_error "Ops! Docker daemon is not running. Start docker first please.\n\t- For Linux, exec 'systemctl start docker' start docker.\n\t- For MacOS, start the Docker Desktop APP.\n\t- And re-exec this script."
            exit 1
        fi
    elif [ "${OS_TYPE}" = "Linux" ]; then
        if [ "$LANG" == "zh_CN.UTF-8" ]; then
            send_warn "未检测到 Docker 环境, 即将自动安装 Docker...\n"
        else
            send_warn "Ops! Docker has not been installed.\nDocker is going to be automatically installed...\n"
        fi
        sleep 3
        curl -sfL https://get.rainbond.com/install_docker | bash
        if [ "$?" != "0" ]; then
            if [ "$LANG" == "zh_CN.UTF-8" ]; then
                send_error "自动安装 Docker 失败！请自行手动安装 Docker 后重新执行本脚本."
                exit 1
            else
                send_error "Ops! Automatic docker installation failed."
                exit 1
            fi
        fi
    elif [ "${OS_TYPE}" = "Darwin" ]; then
        if [ "$LANG" == "zh_CN.UTF-8" ]; then
            send_warn "未检测到 Docker 环境, 请先安装 Docker Desktop APP, 然后重新执行本脚本."
            exit 1
        else
            send_warn "Ops! Docker has not been installed.\nPlease visit the following website to get the latest Docker Desktop APP.\n\thttps://www.docker.com/products/docker-desktop/"
            exit 1
        fi
    fi
else
    if docker ps -a | grep rainbond-allinone 2>&1 >/dev/null; then
        if [ "$LANG" == "zh_CN.UTF-8" ]; then
            send_error "rainbond-allinone 容器已存在.\n\t- 确保 rainbond-allinone 是否在运行.\n\t- 尝试执行 'docker start rainbond-allinone' 命令启动.\n\t- 或者你可以选择删除该容器 'docker rm -f rainbond-allinone'"
            exit 1
        else
            send_error "Ops! rainbond-allinone container already exists.\n\t- Ensure if rainbond-allinone is running.\n\t- Try to exec 'docker start rainbond-allinone' to start it.\n\t- Or you can remove it by 'docker rm -f rainbond-allinone'"
            exit 1
        fi
    fi
fi

ports=(80 443 6060 7070)
for port in ${ports[@]}; do
    if (curl -s 127.0.0.1:$port >/dev/null); then
        if [ "$LANG" == "zh_CN.UTF-8" ]; then
            send_error "$port 端口已被占用."
            exit 1
        else
            send_error "Ops! Port $port has been used."
            exit 1
        fi
    fi
done

########################################
# Arch Detect
# Automatically check the CPU architecture type.
# Return amd64 or arm64.
########################################

if [ $(arch) = "x86_64" ] || [ $(arch) = "amd64" ]; then
    ARCH_TYPE=amd64
elif [ $(arch) = "aarch64" ] || [ $(arch) = "arm64" ]; then
    ARCH_TYPE=arm64
elif [ $(arch) = "i386" ]; then
    ARCH_TYPE=amd64
    if [ "$LANG" == "zh_CN.UTF-8" ]; then
        send_warn "检测到 i386, 我们把它当做 x86_64(amd64). 如果您使用的是 M1 芯片的 MacOS, 确保您禁用了 Rosetta. \n\t 请参阅: https://github.com/goodrain/rainbond/issues/1439 "
    else
        send_warn "i386 has been detect, we'll treat it like x86_64(amd64). If you are using the M1 chip MacOS,make sure your terminal has Rosetta disabled.\n\t Have a look : https://github.com/goodrain/rainbond/issues/1439 "
    fi
else
    if [ "$LANG" == "zh_CN.UTF-8" ]; then
        send_error "Rainbond 目前还不支持 $(arch) 架构"
        exit 1
    else
        send_error "Rainbond do not support $(arch) architecture"
        exit 1
    fi
fi

########################################
# EIP Detect
# Automatically check the IP address.
# User customization is also supported.
########################################

# Choose tool for IP detect.
if which ip >/dev/null; then
    IF_NUM=$(ip -4 a | egrep -v "docker0|flannel|cni|calico|kube" | grep inet | wc -l)
    IPS=$(ip -4 a | egrep -v "docker0|flannel|cni|calico|kube" | grep inet | awk '{print $2}' | awk -F '/' '{print $1}' | tr '\n' ' ')
elif which ifconfig >/dev/null; then
    IF_NUM=$(ifconfig | grep -w inet | awk '{print $2}' | wc -l)
    IPS=$(ifconfig | grep -w inet | awk '{print $2}')
elif which ipconfig >/dev/null; then
    # TODO
    IF_NUM=$(ipconfig ifcount)
    IPS=""
else
    IF_NUM=0
    IPS=""
fi

# Func for verify the result entered.
function verify_eip() {
    local result=$2
    local max=$1
    if [ -z $result ]; then
        echo -e "${YELLOW}Do not enter null values${NC}"
        return 1
    # Regular matching IPv4
    elif [[ $result =~ ^([0-9]{1,2}|1[0-9][0-9]|2[0-4][0-9]|25[0-5]).([0-9]{1,2}|1[0-9][0-9]|2[0-4][0-9]|25[0-5]).([0-9]{1,2}|1[0-9][0-9]|2[0-4][0-9]|25[0-5]).([0-9]{1,2}|1[0-9][0-9]|2[0-4][0-9]|25[0-5])$ ]]; then
        export EIP=$result
        return 0
    # Regular matching positive integer
    elif [[ $result =~ \d? ]]; then
        if [ $result -gt 0 ] && [ $result -le $max ]; then
            export EIP=${ip_list[$result - 1]}
            return 0
        else
            return 1
        fi
    else
        return 1
    fi
}

# The user chooses the IP address to use
if [ -n "$IPS" ]; then
    # Convert to indexed array
    declare -a ip_list=$(echo \($IPS\))

    # Gave some tips
    if [ "$LANG" == "zh_CN.UTF-8" ]; then
        echo -e ${GREEN}
        cat <<EOF
###############################################
# 脚本将自动检测到系统中存在的 IP 地址
# 您可以通过输入序号来选择一个 IP 地址
# 如果您有公网 IP 地址, 直接输入即可
###############################################
 
检测到以下IP:
EOF
        echo -e ${NC}
    else
        echo -e ${GREEN}
        cat <<EOF
###############################################
# The script automatically detects IP addresses in the system
# You can choose one by enter its index
# If you have an Public IP, Just type it in
###############################################
 
The following IP has been detected:
EOF
        echo -e ${NC}
    fi
    for ((i = 1; i <= $IF_NUM; i++)); do
        echo -e "\t${GREEN}$i${NC} : ${ip_list[$i - 1]}"
    done

    for i in 1 2 3; do
        if [ "$LANG" == "zh_CN.UTF-8" ]; then
            echo -e "\n${GREEN}例如: 输入 '1 or 2' 选择 IP, 或指定IP '11.22.33.44'(IPv4 address), 直接回车则使用默认 IP 地址${NC}"
            verify_eip $IF_NUM 2
            echo -n -e "输入您的选择或指定 IP 地址(默认IP是: $EIP):"
        else
            echo -e "\n${GREEN}For example: enter '1 or 2' to choose the IP, or input '11.22.33.44'(IPv4 address) for specific one, press enter to use the default IP address${NC}"
            verify_eip $IF_NUM 2
            echo -n -e "Enter your choose or a specific IP address( Default IP is $EIP):"
        fi
        read res
        if [ -z $res ]; then
            verify_eip $IF_NUM 2 && break
        else
            verify_eip $IF_NUM $res && break
        fi
        if [ "$LANG" == "zh_CN.UTF-8" ]; then
            echo -e "${RED}输入错误, 请重新输入${NC}"
        else
            echo -e "${RED}Incorrect input, please try again${NC}"
        fi
        if [ "$i" = "3" ]; then
            if [ "$LANG" == "zh_CN.UTF-8" ]; then
                send_error "输入错误超过3次, 中止安装"
                exit 1
            else
                send_error "The input error exceeds 3 times, aborting"
                exit 1
            fi
        fi
    done
else
    # Gave some tips
    if [ "$LANG" == "zh_CN.UTF-8" ]; then
        echo -e ${YELLOW}
        cat <<EOF
###############################################
# 自定检测 IP 失败
# 您必须指定一个 IP
# 例如: 
#   您可以输入 "11.22.33.44" 来指定一个 IP
###############################################
EOF
        echo -e ${NC}
    else
        echo -e ${YELLOW}
        cat <<EOF
###############################################
# Failed to automatically detect IP
# You have to specify your own IP
# For example: 
#   you can enter "11.22.33.44" for specific one
###############################################
EOF
        echo -e ${NC}
    fi
    for i in 1 2 3; do
        if [ "$LANG" == "zh_CN.UTF-8" ]; then
            echo -n -e "请输入您的 IP 地址:"
        else
            echo -n -e "Enter your IP address:"
        fi
        read RES
        verify_eip $IF_NUM $RES && break
        if [ "$LANG" == "zh_CN.UTF-8" ]; then
            echo -e "${RED}输入错误, 请重新输入${NC}"
        else
            echo -e "${RED}Incorrect input, please try again${NC}"
        fi
        if [ "$i" = "3" ]; then
            if [ "$LANG" == "zh_CN.UTF-8" ]; then
                send_error "输入错误超过3次, 中止安装"
                exit 1
            else
                send_error "The input error exceeds 3 times, aborting"
                exit 1
            fi
        fi
    done
fi

################## Main ################
# Start install rainbond-dind-allinone
# Automatically generate install cmd with envs
########################################

# Gave some info
if [ "$LANG" == "zh_CN.UTF-8" ]; then
    echo -e ${GREEN}
    cat <<EOF
###############################################
# Rainbond 版本: $RAINBOND_VERSION
# 架构: $ARCH_TYPE
# 操作系统: $OS_TYPE
# Web 控制台访问地址: http://$EIP:7070
# Rainbond 文档: https://www.rainbond.com/docs
# 如过您安装遇到问题，请反馈至:
#     https://www.rainbond.com/community/support
###############################################

EOF
    echo -e "${NC}"
else
    echo -e ${GREEN}
    cat <<EOF
###############################################
# Rainbond dind allinone will be installed with:
# Rainbond Version: $RAINBOND_VERSION
# Arch: $ARCH_TYPE
# OS: $OS_TYPE
# Web Site: http://$EIP:7070
# Rainbond Docs: https://www.rainbond.com/docs
# If you install problem, please feedback to: 
#     https://www.rainbond.com/community/support
###############################################

EOF
    echo -e "${NC}"
fi

if [ "$LANG" == "zh_CN.UTF-8" ]; then
    echo -e "${GREEN}生成安装命令:${NC}"
    sleep 3
else
    echo -e "${GREEN}Generating the installation command:${NC}"
    sleep 3
fi

# Generate the installation command based on the detect results
if [ "$OS_TYPE" = "Linux" ]; then
    if [ "$ARCH_TYPE" = "amd64" ]; then
        VOLUME_OPTS="-v ~/.ssh:/root/.ssh -v ~/rainbonddata:/app/data -v /opt/rainbond:/opt/rainbond"
        RBD_IMAGE="${IMGHUB_MIRROR}/rainbond:${RAINBOND_VERSION}-dind-allinone"
    elif [ "$ARCH_TYPE" = "arm64" ]; then
        VOLUME_OPTS="-v ~/.ssh:/root/.ssh -v ~/rainbonddata:/app/data -v /opt/rainbond:/opt/rainbond"
        RBD_IMAGE="${IMGHUB_MIRROR}/rainbond:${RAINBOND_VERSION}-arm64-dind-allinone"
    fi
elif [ "$OS_TYPE" = "Darwin" ]; then
    if [ "$ARCH_TYPE" = "amd64" ]; then
        VOLUME_OPTS="-v ~/.ssh:/root/.ssh -v rainbond-data:/app/data -v rainbond-opt:/opt/rainbond"
        RBD_IMAGE="${IMGHUB_MIRROR}/rainbond:${RAINBOND_VERSION}-dind-allinone"
    elif [ "$ARCH_TYPE" = "arm64" ]; then
        VOLUME_OPTS="-v ~/.ssh:/root/.ssh -v rainbond-data:/app/data -v rainbond-opt:/opt/rainbond"
        RBD_IMAGE="${IMGHUB_MIRROR}/rainbond:${RAINBOND_VERSION}-arm64-dind-allinone"
    fi
fi

# Generate cmd
docker_run_cmd="docker run --privileged -d -p 7070:7070 -p 80:80 -p 443:443 -p 6060:6060 -p 10000-10010:10000-10010 --name=rainbond-allinone --restart=on-failure \
${VOLUME_OPTS} -e EIP=$EIP -e UUID=${UUID} ${RBD_IMAGE}"
send_info "$docker_run_cmd"

# Pull image
if [ "$LANG" == "zh_CN.UTF-8" ]; then
    send_info "获取镜像中 ${RBD_IMAGE}..."
else
    send_info "Pulling image ${RBD_IMAGE}..."
fi
if docker pull ${RBD_IMAGE}; then
    rbd_image_id=$(docker images | grep dind-allinone | grep ${RAINBOND_VERSION} | awk '{print $3}')
    if [ "$LANG" == "zh_CN.UTF-8" ]; then
        send_info "Rainbond 容器 ID 为: ${rbd_image_id}"
    else
        send_info "Rainbond container ID is: ${rbd_image_id}"
    fi
else
    if [ "$LANG" == "zh_CN.UTF-8" ]; then
        send_error "获取镜像失败."
    else
        send_error "Pull image failed."
    fi
fi
sleep 3

# Run container
if [ "$LANG" == "zh_CN.UTF-8" ]; then
    send_info "Rainbond dind allinone 正在安装中...\n"
else
    send_info "Rainbond dind allinone distribution is installing...\n"
fi
docker_run_meg=$(bash -c "$docker_run_cmd" 2>&1)
send_info "$docker_run_meg"
sleep 3

# Verify startup
container_id=$(docker ps -a | grep rainbond-allinone | awk '{print $1}')
if docker ps | grep rainbond-allinone 2>&1 >/dev/null; then
    if [ "$LANG" == "zh_CN.UTF-8" ]; then
        send_info "Rainbond dind allinone 启动成功，容器 ID 为: $container_id. 请观察 rainbond-allinone 容器启动日志.\n"
    else
        send_info "Rainbond dind allinone container startup succeeded with $container_id. Please observe rainbond-allinone container startup logs.\n"
    fi
else
    if [ "$LANG" == "zh_CN.UTF-8" ]; then
        send_warn "Rainbond dind allinone 容器启动失败. 请查看 rainbond-allinone 容器启动日志.\n"
    else
        send_warn "Ops! Rainbond dind allinone container startup failed. please observe rainbond-allinone container startup logs.\n"
    fi
    send_msg "$(docker logs rainbond-allinone)" # Msg maybe too lang
fi

sleep 3
# Follow logs stdout
docker logs -f rainbond-allinone