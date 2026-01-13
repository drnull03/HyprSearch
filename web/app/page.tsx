import { SearchBar } from "@/components/search-bar"
import { ResultsTable } from "@/components/results-table"

export default function Home() {
  return (
    <main className="min-h-screen bg-background flex flex-col items-center justify-center p-6">
      <div className="w-full max-w-4xl space-y-8">
        <SearchBar />
        <ResultsTable />
      </div>
    </main>
  )
}
