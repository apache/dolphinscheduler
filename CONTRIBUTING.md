* First from the remote repository *https://github.com/analysys/EasyScheduler.git* fork code to your own repository

* there are three branches in the remote repository currently:
     * master normal delivery branch
            After the stable version is released, the code for the stable version branch is merged into the master branch.

    * dev daily development branch
            The daily development branch, the newly submitted code can pull requests to this branch.

    * branch-1.0.0 release version branch
            Release version branch, there will be 2.0 ... and other version branches, the version 
            branch only changes the error, does not add new features.

* Clone your own warehouse to your local

    `git clone https://github.com/analysys/EasyScheduler.git`

* Add remote repository address, named upstream

   `git remote add upstream https://github.com/analysys/EasyScheduler.git`

* View repository:

    `git remote -v`

> There will be two repositories at this time: origin (your own warehouse) and upstream (remote repository)

* Get/update remote repository code (already the latest code, skip it)

   `git fetch upstream`


* Synchronize remote repository code to local repository

```
git checkout origin/dev
git merge --no-ff upstream/dev
```

If remote branch has a new branch `DEV-1.0`, you need to synchronize this branch to the local repository.

```
git checkout -b dev-1.0 upstream/dev-1.0
git push --set-upstream origin dev1.0
```

* After modifying the code locally, submit it to your own repository:

`git commit -m 'test commit'`
`git push`

* Submit changes to the remote repository

* On the github page, click on the new pull request.
<p align = "center">
<img src = "http://geek.analysys.cn/static/upload/221/2019-04-02/90f3abbf-70ef-4334-b8d6-9014c9cf4c7f.png"width ="60%"/>
</ p>

* Select the modified local branch and the branch to merge past to create a pull request.
<p align = "center">
<img src = "http://geek.analysys.cn/static/upload/221/2019-04-02/fe7eecfe-2720-4736-951b-b3387cf1ae41.png"width ="60%"/>
</ p>

*  Next, the administrator is responsible for **merging** to complete the pull request

---

* 首先从远端仓库*https://github.com/analysys/EasyScheduler.git* fork一份代码到自己的仓库中

* 远端仓库中目前有三个分支：
    * master 正常交付分支
	   发布稳定版本以后，将稳定版本分支的代码合并到master上。
    
	* dev    日常开发分支
	   日常dev开发分支，新提交的代码都可以pull request到这个分支上。
	   
    * branch-1.0.0 发布版本分支
	   发布版本分支，后续会有2.0...等版本分支，版本分支只修改bug，不增加新功能。

* 把自己仓库clone到本地
  
    `git clone https://github.com/analysys/EasyScheduler.git`

*  添加远端仓库地址，命名为upstream

    ` git remote add upstream https://github.com/analysys/EasyScheduler.git `

*  查看仓库：

    ` git remote -v`

> 此时会有两个仓库：origin(自己的仓库)和upstream（远端仓库）

*  获取/更新远端仓库代码（已经是最新代码，就跳过）
  
    `git fetch upstream `


* 同步远端仓库代码到本地仓库

```
 git checkout origin/dev
 git merge --no-ff upstream/dev
```

如果远端分支有新加的分支`dev-1.0`,需要同步这个分支到本地仓库

```
git checkout -b dev-1.0 upstream/dev-1.0
git push --set-upstream origin dev1.0
```

* 在本地修改代码以后，提交到自己仓库：
  
    `git commit -m 'test commit'`
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










