// src/lib/api.ts
import axios, { AxiosError } from "axios";

const ACCESS_TOKEN_KEY = "jwt";
const USERNAME_KEY = "auth_username";

// 백엔드 API와 통신하기 위한 axios 인스턴스를 생성한다.
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE,
  headers: { "Content-Type": "application/json" },
});

// 세션 저장소에 담긴 액세스 토큰을 읽어온다.
function getAccessToken() {
  return sessionStorage.getItem(ACCESS_TOKEN_KEY);
}

// 최근 로그인한 사용자의 아이디(username/email)를 저장한다.
function getStoredUsername() {
  return sessionStorage.getItem(USERNAME_KEY);
}

function setAccessToken(token: string | null) {
  if (token) sessionStorage.setItem(ACCESS_TOKEN_KEY, token);
  else sessionStorage.removeItem(ACCESS_TOKEN_KEY);
}

function setStoredUsername(username: string | null) {
  if (username) sessionStorage.setItem(USERNAME_KEY, username);
  else sessionStorage.removeItem(USERNAME_KEY);
}

// 로그아웃 시 토큰과 사용자 정보를 함께 비운다.
function clearTokens() {
  setAccessToken(null);
  setStoredUsername(null);
}

// 모든 요청에 액세스 토큰이 있으면 Authorization 헤더를 자동으로 붙인다.
api.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      clearTokens();
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }
    throw error;
  }
);

export {
  api,
  getAccessToken,
  setAccessToken,
  getStoredUsername,
  setStoredUsername,
  clearTokens,
};
