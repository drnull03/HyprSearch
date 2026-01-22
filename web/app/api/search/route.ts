import { NextResponse } from 'next/server';
import zookeeper from 'node-zookeeper-client';

const ZK_ADDRESS = 'localhost:2181';
const LEADER_PATH = '/leader_registry/leader';


async function fetchCoordinatorAddress(client: zookeeper.Client): Promise<string> {
    return new Promise((resolve, reject) => {
        client.getData(LEADER_PATH, (error, data) => {
            if (error) {
                return reject(error);
            }
            if (!data) {
                return reject(new Error("No leader data found in ZooKeeper"));
            }
            resolve(data.toString());
        });
    });
}


async function connectZK(client: zookeeper.Client): Promise<void> {
    return new Promise((resolve, reject) => {
        client.once('connected', resolve);
        client.once('error', reject);
        client.connect();
    });
}

export async function POST(request: Request) {
    const { query } = await request.json();
    const client = zookeeper.createClient(ZK_ADDRESS);

    try {
        await connectZK(client);

        
        let coordinatorAddress = await fetchCoordinatorAddress(client);

        try {
           
            const response = await fetch(`${coordinatorAddress}/search`, {
                method: 'POST',
                body: query,
                
                signal: AbortSignal.timeout(3000) 
            });

            if (!response.ok) throw new Error("Leader unresponsive");

            const results = await response.json();
            return NextResponse.json(results);

        } catch (searchError) {
            console.warn("Primary leader failed, attempting re-discovery...");

          
            await new Promise(resolve => setTimeout(resolve, 1500));

            
            coordinatorAddress = await fetchCoordinatorAddress(client);
            
            console.info("New leader found at:", coordinatorAddress);

            const retryResponse = await fetch(`${coordinatorAddress}/search`, {
                method: 'POST',
                body: query,
            });

            const results = await retryResponse.json();
            return NextResponse.json(results);
        }

    } catch (error: any) {
        console.error('Distributed System Error:', error);
        return NextResponse.json(
            { error: 'Search cluster is currently re-electing a leader. Please try again.' }, 
            { status: 503 }
        );
    } finally {
        client.close();
    }
}