# Dolphin Scheduler UI Next

> After two and a half months of development cycle, we have brought a brand-new `UI` management system (V1.0.0-Alpha).
>
> Compared with the old `UI`, it will be more standardized, and it will also have a more complete type checking mechanism. At the same time, its speed has made a qualitative leap.
>
> We also provide dark mode and light mode to meet the preferences of different developers. It will make your eyes shine.
>
> If you have tried the new `UI` and found problems in use, you can contact us through issue.

---

### Start Using

For the best experience, we recommend using `node 16.x.x` and `pnpm 6.x.x`.
You can learn how to install the corresponding version from their official website.

- [node](https://nodejs.org/en/)
- [pnpm](https://pnpm.io/)

#### Install Dependencies

```shell
pnpm install
```

#### Development Project

To do this you need to change the `VITE_APP_DEV_WEB_URL` parameter variable in `.env.development` in the project root directory.

It is worth noting that when you do not change the request path or route, you only need to write `http`, `ip` and `port` without the `/` symbol at the end, such as `http://127.0. 0.1:12345`.

```shell
pnpm run dev
```

#### Build Project

When you are ready to package, you need to modify the `VITE_APP_PROD_WEB_URL` parameter in `.env.production` accordingly to ensure that the packaged file can be normally requested to the backend service address.

```shell
pnpm run build:prod
```

#### Code Format

Usually after you modify the code, you need to perform code formatting operations to ensure that the code in the project is the same style.

```shell
pnpm run prettier
```

---

### E2E

In order to ensure the stability of the project, we have added a lot of `E2E` tests, you can learn about it through the following list, and when you make code changes, please make sure that the `DOM` of `E2E` has not changed, if there are changes, please Correspondingly modify the backend code or explain it in `PR`.

- [Login](https://github.com/apache/dolphinscheduler/tree/dev/dolphinscheduler-ui-next/docs/e2e/login.md)
- [Data Source](https://github.com/apache/dolphinscheduler/tree/dev/dolphinscheduler-ui-next/docs/e2e/data-source.md)
- [Navbar](https://github.com/apache/dolphinscheduler/tree/dev/dolphinscheduler-ui-next/docs/e2e/navbar.md)
- [Project](https://github.com/apache/dolphinscheduler/tree/dev/dolphinscheduler-ui-next/docs/e2e/project.md)
- [Resource](https://github.com/apache/dolphinscheduler/tree/dev/dolphinscheduler-ui-next/docs/e2e/resource.md)
- [Security](https://github.com/apache/dolphinscheduler/tree/dev/dolphinscheduler-ui-next/docs/e2e/security.md)

### Known Issues

Since this system is newly developed, there are some known issues, but this does not affect your use. We will deal with it in subsequent versions. If you are an open source enthusiast, your contributions are very welcome.

Check out all known issues [Know Issues](https://github.com/apache/dolphinscheduler/issues/8565).
