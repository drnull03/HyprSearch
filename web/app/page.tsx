"use client"

import { useState } from "react"
import { SearchBar } from "@/components/search-bar"
import { ResultsTable } from "@/components/results-table"

// Define the shape of a single search result
interface SearchResult {
  doc: string;
  score: number;
}

export default function Home() {
  //  Explicitly tell useState that this is an array of SearchResult
  const [results, setResults] = useState<SearchResult[]>([])
  const [loading, setLoading] = useState(false)

  const handleSearch = async (query: string) => {
    if (!query) return;
    setLoading(true);
    try {
      const response = await fetch('/api/search', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ query })
      });
      
      const data = await response.json();
      
      //  Now TypeScript knows 'data' matches SearchResult[]
      setResults(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error("Search failed", error);
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="min-h-screen bg-background flex flex-col items-center justify-center p-6">
      <div className="w-full max-w-4xl space-y-8">
        <div className="text-center space-y-2">
          <h1 className="text-4xl font-bold tracking-tight">HyprSearch</h1>
          <p className="text-muted-foreground">Distributed TF-IDF Search Engine</p>
        </div>
        <SearchBar onSearch={handleSearch} />
        <ResultsTable results={results} loading={loading} />
      </div>
    </main>
  )
}