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