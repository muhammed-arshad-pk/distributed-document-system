# Distributed Document Editing System (P2P)

A peer-to-peer distributed document editor implemented in Java using Swing and TCP sockets.

Each node acts as both a client and a server, maintains a local replica of the document, and synchronizes updates with other peers. The system demonstrates core distributed systems concepts such as replication, eventual consistency, and decentralized control.

---

## 🔑 Key Features

- Peer-to-peer architecture (no central server)
- GUI-based document editor (Swing)
- Local document replication on each node
- Manual edit, update, and lock control
- Eventual consistency (last-write-wins)
- TCP socket-based communication
- Local persistence using file system

---

## 🧠 Distributed Systems Concepts Used

- Peer-to-peer model
- Replicated state
- Eventual consistency
- Fault tolerance (no single point of failure)
- Symmetric nodes

---

## ⚙️ How to Run

### 1. Compile
```bash
javac src/DistributedDocumentNode.java
