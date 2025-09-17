const API_BASE = "http://localhost:8080";

async function handle<T>(res: Response): Promise<T> {
    if (!res.ok) {
        const msg = `API error: ${res.status}`;
        throw new Error(msg);
    }
    return res.json();
}

export async function apiGet<T>(path: string): Promise<T> {
    const res = await fetch(`${API_BASE}${path}`);
    return handle<T>(res);
}

export async function apiPut<T>(path: string): Promise<T> {
    const res = await fetch(`${API_BASE}${path}`, { method: "PUT" });
    return handle<T>(res);
}

export async function apiDelete<T>(path: string): Promise<T> {
    const res = await fetch(`${API_BASE}${path}`, { method: "DELETE" });
    return handle<T>(res);
}
