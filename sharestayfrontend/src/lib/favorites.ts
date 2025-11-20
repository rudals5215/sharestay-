import { api } from "./api";
import type { FavoriteRoom } from "../types/favorite";

export const fetchFavoriteRooms = async (userId: number) => {
  const { data } = await api.get<FavoriteRoom[]>("/favorites/list", {
    params: { userId },
  });
  return Array.isArray(data) ? data : [];
};

export const toggleFavoriteRoom = async (userId: number, roomId: number) => {
  await api.post(
    "/favorites/toggle",
    {},
    {
      params: { userId, roomId },
    }
  );
};
