import { createContext, useContext, useEffect, useState } from "react";
import { http, tokenStore } from "../api/http";

// decode JWT (no crypto validation, just UI convenience)
function decodeJwt(token) {
  try {
    const [, payload] = token.split(".");
    return JSON.parse(atob(payload.replace(/-/g, "+").replace(/_/g, "/")));
  } catch { return null; }
}

const AuthCtx = createContext(null);
export const useAuth = () => useContext(AuthCtx);

export default function AuthProvider({ children }) {
  const [token, setToken] = useState(tokenStore.get());
  const [user, setUser] = useState(() => (token ? decodeJwt(token) : null));

  useEffect(() => {
    if (token) {
      tokenStore.set(token);
      setUser(decodeJwt(token));
    } else {
      tokenStore.clear();
      setUser(null);
    }
  }, [token]);

  async function login(username, password) {
    const { data } = await http.post("/auth/login", { username, password });
    setToken(data.token);
    return data;
  }

  function logout() {
    setToken(null);
  }

  return (
    <AuthCtx.Provider value={{ token, user, login, logout, isAuthed: !!token }}>
      {children}
    </AuthCtx.Provider>
  );
}
