"use client";

import { useEffect, useState } from "react";

export default function Home() {
  const [message, setMessage] = useState("");

  useEffect(() => {
    fetch("http://localhost:8098/api/hello") // make sure this matches your backend port
        .then((res) => res.text())
        .then((data) => setMessage(data))
        .catch((err) => console.error("Error:", err));
  }, []);

  return (
      <div>
        <h1>Response from Backend:</h1>
        <p>{message}</p>
      </div>
  );
}