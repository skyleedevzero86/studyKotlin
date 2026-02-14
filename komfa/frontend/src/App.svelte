<script lang="ts">
  import "./App.css";
  import { onMount } from "svelte";
  import Login from "./ui/svelte/Login.svelte";
  import OttSent from "./ui/svelte/OttSent.svelte";
  import Join from "./ui/svelte/Join.svelte";
  import FindUsername from "./ui/svelte/FindUsername.svelte";
  import ForgotPassword from "./ui/svelte/ForgotPassword.svelte";
  import ResetPassword from "./ui/svelte/ResetPassword.svelte";
  import Home from "./ui/svelte/Home.svelte";
  import Me from "./ui/svelte/Me.svelte";
  import User from "./ui/svelte/User.svelte";
  import Admin from "./ui/svelte/Admin.svelte";
  import * as api from "./infrastructure/http/api";

  let path = $state(
    typeof window !== "undefined" ? window.location.pathname : "/",
  );
  let profile = $state<api.MeResponse | null>(null);
  let isAdmin = $state(false);

  function goto(p: string) {
    if (typeof window === "undefined") return;
    window.history.pushState(null, "", p);
    path = p;
  }

  onMount(() => {
    function update() {
      path = window.location.pathname;
    }
    window.addEventListener("popstate", update);
    return () => window.removeEventListener("popstate", update);
  });

  $effect(() => {
    if (path === "/" || path === "/login") {
      api.me().then((p) => {
        profile = p;
        if (p)
          api.adminArea().then((a) => {
            isAdmin = a !== null;
          });
        else isAdmin = false;
      });
    }
  });

  function handleOttSent() {
    goto("/ott/sent");
  }
</script>

<main class="app">
  {#if path === "/join"}
    <Join />
    <a
      href="/"
      class="app-link"
      onclick={(e) => {
        e.preventDefault();
        goto("/");
      }}>로그인</a
    >
  {:else if path === "/find-username"}
    <FindUsername />
    <a
      href="/"
      class="app-link"
      onclick={(e) => {
        e.preventDefault();
        goto("/");
      }}>로그인</a
    >
  {:else if path === "/forgot-password"}
    <ForgotPassword />
    <a
      href="/"
      class="app-link"
      onclick={(e) => {
        e.preventDefault();
        goto("/");
      }}>로그인</a
    >
  {:else if path === "/reset-password"}
    <ResetPassword />
    <a
      href="/"
      class="app-link"
      onclick={(e) => {
        e.preventDefault();
        goto("/");
      }}>로그인</a
    >
  {:else if path === "/ott/sent"}
    <OttSent onBack={() => goto("/")} />
    <a
      href="/"
      class="app-link"
      onclick={(e) => {
        e.preventDefault();
        goto("/");
      }}>로그인</a
    >
  {:else if path === "/me"}
    <Me />
    <a
      href="/"
      class="app-link"
      onclick={(e) => {
        e.preventDefault();
        goto("/");
      }}>홈</a
    >
  {:else if path === "/user"}
    <User />
    <a
      href="/"
      class="app-link"
      onclick={(e) => {
        e.preventDefault();
        goto("/");
      }}>홈</a
    >
  {:else if path === "/admin"}
    <Admin />
    <a
      href="/"
      class="app-link"
      onclick={(e) => {
        e.preventDefault();
        goto("/");
      }}>홈</a
    >
  {:else if path === "/" || path === "/login"}
    {#if profile}
      <Home {profile} {isAdmin} {goto} />
    {:else}
      <Login onOttSent={handleOttSent} />
      <nav class="app-nav">
        <a
          href="/join"
          class="app-link"
          onclick={(e) => {
            e.preventDefault();
            goto("/join");
          }}>회원가입</a
        >
        <a
          href="/find-username"
          class="app-link"
          onclick={(e) => {
            e.preventDefault();
            goto("/find-username");
          }}>아이디 찾기</a
        >
        <a
          href="/forgot-password"
          class="app-link"
          onclick={(e) => {
            e.preventDefault();
            goto("/forgot-password");
          }}>비밀번호 찾기</a
        >
      </nav>
    {/if}
  {:else}
    <p>페이지를 찾을 수 없습니다.</p>
    <a
      href="/"
      class="app-link"
      onclick={(e) => {
        e.preventDefault();
        goto("/");
      }}>홈</a
    >
  {/if}
</main>
