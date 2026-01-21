import { NextResponse } from 'next/server';
import zookeeper from 'node-zookeeper-client';

const ZK_ADDRESS = 'localhost:2181';
const LEADER_PATH = '/leader_registry/leader';

export async function POST(request: Request) {
    const { query } = await request.json();
    const client = zookeeper.createClient(ZK_ADDRESS);

    try {
        // connect to zookeeper
        await new Promise((resolve, reject) => {
            client.once('connected', resolve);
            client.once('error', reject);
            client.connect();
        });

        //  Get Coordinator Address
        const coordinatorAddress: string = await new Promise((resolve, reject) => {
            client.getData(LEADER_PATH, (error, data) => {
                if (error) return reject(error);
                resolve(data.toString());
            });
        });

        client.close();

        //  Forward request to Coordinator
        const response = await fetch(`${coordinatorAddress}/search`, {
            method: 'POST',
            body: query,
        });

        const results = await response.json();
        return NextResponse.json(results);

    } catch (error: any) {
        console.error('ZooKeeper/Coordinator error:', error);
        return NextResponse.json({ error: 'Search service unavailable' }, { status: 500 });
    }
}