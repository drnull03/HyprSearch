"use client"

import { Search } from "lucide-react"
import { Input } from "@/components/ui/input"

interface SearchBarProps {
  onSearch: (query: string) => void;
}

export function SearchBar({ onSearch }: SearchBarProps) {
  return (
    <div className="relative w-full">
      <div className="absolute inset-y-0 left-4 flex items-center pointer-events-none">
        <Search className="h-5 w-5 text-muted-foreground" />
      </div>
      <Input
        type="search"
        placeholder="Search documents (e.g., physics)..."
        className="w-full h-14 pl-12 pr-4 text-lg bg-card border-border focus:border-primary"
        onKeyDown={(e) => {
          if (e.key === 'Enter') onSearch(e.currentTarget.value);
        }}
      />
    </div>
  )
}