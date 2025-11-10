// src/auth/AuthContext.tsx
import React, { createContext, useEffect, useMemo, useState } from "react";
import {
  api,
  clearTokens,
  getAccessToken,
  getStoredUsername,
  setAccessToken,
  setStoredUsername,
} from "../lib/api";
import type {
  LoginResponse,
  Roles,
  SignupPayload,
  UpdateProfilePayload,
  UserInfo,
} from "./types";

type BackendUser = {
  id: number;
  username: string;
  nickname?: string;
  role?: string;
  address?: string;
  phoneNumber?: string;
  lifeStyle?: string;
  signupDate?: string;
  hostIntroduction?: string;
  hostTermsAgreed?: boolean;
};

type AuthContextType = {
  user: UserInfo | null;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  signup: (payload: SignupPayload) => Promise<void>;
  logout: () => void;
  refreshProfile: () => Promise<void>;
  updateProfile: (payload: UpdateProfilePayload) => Promise<void>;
};

const noop = async () => {};

export const AuthContext = createContext<AuthContextType>({
  user: null,
  isLoading: true,
  login: noop,
  signup: noop,
  logout: () => undefined,
  refreshProfile: noop,
  updateProfile: noop,
});

function toRoles(role?: string): Roles[] {
  if (!role) return [];
  const normalized = role.toUpperCase() as Roles;
  return [normalized];
}

function mapUser(dto: BackendUser): UserInfo {
  return {
    id: dto.id,
    username: dto.username,
    nickname: dto.nickname,
    email: dto.username,
    role: dto.role?.toUpperCase() as Roles | undefined,
    roles: toRoles(dto.role),
    address: dto.address,
    phoneNumber: dto.phoneNumber,
    lifeStyle: dto.lifeStyle,
    signupDate: dto.signupDate,
    hostIntroduction: dto.hostIntroduction,
    hostTermsAgreed: dto.hostTermsAgreed,
  };
}

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [user, setUser] = useState<UserInfo | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const fetchProfile = async (username: string) => {
    const { data } = await api.get<BackendUser>(`/users/${username}`);
    setUser(mapUser(data));
  };

  useEffect(() => {
    const token = getAccessToken();
    const username = getStoredUsername();
    if (!token || !username) {
      setIsLoading(false);
      return;
    }

    fetchProfile(username)
      .catch(() => {
        clearTokens();
        setUser(null);
      })
      .finally(() => setIsLoading(false));
  }, []);

  const login = async (username: string, password: string) => {
    const { data } = await api.post<LoginResponse>('/login', {
      username,
      password,
    });
    if (!data?.accessToken) {
      throw new Error('Access token is missing in the response.');
    }

    setAccessToken(data.accessToken);
    setStoredUsername(username);
    await fetchProfile(username);
  };

  const signup = async (payload: SignupPayload) => {
    await api.post('/signup', payload);
  };

  const logout = () => {
    clearTokens();
    setUser(null);
    window.location.href = '/login';
  };

  const refreshProfile = async () => {
    const username = getStoredUsername();
    if (!username) return;
    await fetchProfile(username);
  };

  const updateProfile = async (payload: UpdateProfilePayload) => {
    const username = getStoredUsername();
    if (!username) {
      throw new Error('사용자 정보를 찾을 수 없습니다.');
    }
    await api.put(`/users/${username}`, payload);
    await fetchProfile(username);
  };

  const value = useMemo(
    () => ({
      user,
      isLoading,
      login,
      signup,
      logout,
      refreshProfile,
      updateProfile,
    }),
    [user, isLoading]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

