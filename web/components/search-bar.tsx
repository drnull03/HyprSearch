"use client"

import { Search } from "lucide-react"
import { Input } from "@/components/ui/input"

export function SearchBar() {
  return (
    <div className="relative w-full">
      <div className="absolute inset-y-0 left-4 flex items-center pointer-events-none">
        <Search className="h-5 w-5 text-muted-foreground" />
      </div>
      <Input
        type="search"
        placeholder="Search..."
        className="w-full h-14 pl-12 pr-4 text-lg bg-card border-border focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all"
      />
    </div>
  )
}
