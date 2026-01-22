# HyprSearch: Distributed TF-IDF Search Engine

HyprSearch is a distributed search engine designed to handle large-scale document indexing and querying using the TF-IDF (Term Frequency-Inverse Document Frequency) algorithm. The system utilizes a leader-worker architecture managed by Apache ZooKeeper to ensure high availability and automated failover.

## System Architecture

The project consists of three main components:

1. **Coordinator (Leader):** Acts as the entry point for search queries. It aggregates local statistics from all active workers, calculates global TF-IDF scores, and returns ranked results.
2. **Workers:** Responsible for a specific shard of the dataset. Each worker calculates term frequencies for its local documents and serves them to the coordinator.
3. **ZooKeeper:** Manages leader election and service registration. If the coordinator fails, a worker is automatically promoted to leader, and the search engine continues to function.

## Prerequisites

Before running the project, ensure you have the following installed:

* Java Development Kit (JDK) 17 or higher
* Apache Maven
* Node.js and npm
* Apache ZooKeeper

## Setup and Execution

### 1. Start ZooKeeper

You must have ZooKeeper running before starting any application nodes.

**Download and Extract ZooKeeper**, then navigate to its directory and run:

```bash
# For Linux/Mac
bin/zkServer.sh start

# For Windows
bin\zkServer.cmd

```

By default, the application looks for ZooKeeper at `localhost:2181`.

### 2. Run the Search Cluster

Navigate to the root directory of the Java project (`HyprSearch`) to start the nodes.

**Start the first node (will become Leader):**

```bash
mvn exec:java -Dexec.args="5555"

```

**Start additional nodes (will become Workers):**
Open new terminal tabs for each worker and assign a unique port:

```bash
mvn exec:java -Dexec.args="9001"

```

```bash
mvn exec:java -Dexec.args="9002"

```

The system uses modulo-based sharding, so the documents in the `dataset` folder will be automatically split between the available workers.

### 3. Start the Web Frontend

Navigate to the `web` folder to launch the user interface.

**Install dependencies:**

```bash
npm install

```

**Run the development server:**

```bash
npm run dev

```

### 4. Access the Interface

Open your web browser and navigate to:
`http://localhost:3000`

## How it Works

1. **Data Partitioning:** When a worker starts, it checks its index in the ZooKeeper cluster and loads only its assigned portion of the files from the `dataset` directory.
2. **Query Path:** The frontend sends a request to the Next.js API route. The API route queries ZooKeeper to find the current Leader's address.
3. **Aggregation:** The Leader receives the query, broadcasts it to all registered Workers, and collects their local term frequency data.
4. **Ranking:** The Leader computes the final TF-IDF scores using the global document count and returns a sorted JSON list of the top 10 documents.

## Failover

If the node running on port 5555 is terminated, ZooKeeper will detect the loss of connection. The next worker in the sequence (e.g., 9001) will be notified, shut down its worker service, and restart itself as the new Coordinator. The Next.js API route is designed to retry the connection and locate this new leader automatically.

