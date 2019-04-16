EasyScheduler提交代码流程
=====
* 首先从远端仓库*https://github.com/analysys/EasyScheduler.git* fork一份代码到自己的仓库中

* 远端仓库中目前有三个分支：
    * master 正常交付分支
    * dev    日常开发分支
    * branch-1.0.0 发布版本分支

* 把自己仓库clone到本地
  
    `git clone https://github.com/**/EasyScheduler.git`

*  添加远端仓库地址，命名为upstream

    ` git remote add upstream https://github.com/analysys/EasyScheduler.git `

*  查看仓库：

    ` git remote -v`

> 此时会有两个仓库：origin(自己的仓库)和upstream（远端仓库）

*  获取远端仓库代码（已经是最新代码，就跳过）
  
    `git fetch upstream `

*  更新远端仓库代码

```
git checkout upstream/dev

git pull upstream dev
```

* 同步远端仓库代码到本地仓库

```
 git checkout origin/dev
 git merge --no-ff upstream/dev

```

* 在本地修改代码以后，提交到自己仓库：
  
    `git ca -m 'test commit'`
    `git push`

* 将修改提交到远端仓库

	* 在github页面，点击New pull request.
		<p align="center">
	   <img src="http://geek.analysys.cn/static/upload/221/2019-04-02/90f3abbf-70ef-4334-b8d6-9014c9cf4c7f.png" width="60%" />
	 </p>
	 
	* 选择修改完的本地分支和要合并过去的分支，Create pull request.
		<p align="center">
	   <img src="http://geek.analysys.cn/static/upload/221/2019-04-02/fe7eecfe-2720-4736-951b-b3387cf1ae41.png" width="60%" />
	 </p>
	* 接下来由管理员负责将**Merge**完成此次pull request










