import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Card } from "@/components/ui/card"

interface Result {
  doc: string;
  score: number;
}

export function ResultsTable({ results, loading }: { results: Result[], loading: boolean }) {
  return (
    <Card className="border-border bg-card overflow-hidden">
      <Table>
        <TableHeader>
          <TableRow className="border-border hover:bg-transparent">
            <TableHead className="text-foreground font-semibold">Document Name</TableHead>
            <TableHead className="text-foreground font-semibold text-right">TF-IDF Score</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {loading ? (
            <TableRow><TableCell colSpan={2} className="text-center py-12">Searching cluster...</TableCell></TableRow>
          ) : results.length > 0 ? (
            results.map((r, i) => (
              <TableRow key={i}>
                <TableCell className="font-medium">{r.doc}</TableCell>
                <TableCell className="text-right">{r.score.toFixed(6)}</TableCell>
              </TableRow>
            ))
          ) : (
            <TableRow>
              <TableCell colSpan={2} className="text-center py-12 text-muted-foreground">No results found.</TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </Card>
  )
}