/**
 * Simple E2E test for the chat WS using native WebSocket and STOMP frames (minimal).
 * Run: npm i ws
 *      TS_NODE_TRANSPILE_ONLY=1 npx ts-node scripts/chat-e2e.ts
 */
import WebSocket from "ws";

const WS_URL = process.env.WS_URL || "ws://localhost:8098/ws";
const JWT = process.env.JWT || "<paste a valid jwt>";
const ORG = process.env.ORG_ID || "org_123";
const GROUP = process.env.GROUP_ID || "UNIVERSAL";

function connect(): Promise<WebSocket> {
  return new Promise((resolve, reject) => {
    const ws = new WebSocket(WS_URL);
    ws.on("open", () => {
      // STOMP CONNECT
      ws.send(
        "CONNECT\naccept-version:1.2\nhost:localhost\nAuthorization: Bearer " +
          JWT +
          "\n\n\u0000",
      );
    });
    ws.on("message", (data: any) => {
      const msg = data.toString();
      if (msg.startsWith("CONNECTED")) {
        resolve(ws);
      } else {
        console.log("RECV:", msg);
      }
    });
    ws.on("error", reject);
  });
}

(async () => {
  const ws = await connect();
  // SUBSCRIBE
  const subId = "sub-1";
  const dest = `/topic/org.${ORG}.group.${GROUP}`;
  ws.send(`SUBSCRIBE\nid:${subId}\ndestination:${dest}\n\n\u0000`);

  // SEND message
  const sendDest = `/app/org/${ORG}/group/${GROUP}/send`;
  const body = JSON.stringify({ content: "Hello from E2E!" });
  ws.send(`SEND\ndestination:${sendDest}\ncontent-type:application/json\ncontent-length:${body.length}\n\n${body}\u0000`);

  setTimeout(() => {
    ws.send(`UNSUBSCRIBE\nid:${subId}\n\n\u0000`);
    ws.send("DISCONNECT\n\n\u0000");
    ws.close();
  }, 3000);
})();
