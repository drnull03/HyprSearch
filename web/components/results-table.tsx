import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Card } from "@/components/ui/card"

export function ResultsTable() {
  return (
    <Card className="border-border bg-card overflow-hidden">
      <Table>
        <TableHeader>
          <TableRow className="border-border hover:bg-transparent">
            <TableHead className="text-foreground font-semibold">Name</TableHead>
            <TableHead className="text-foreground font-semibold">Score</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow className="border-0">
            <TableCell colSpan={2} className="text-center py-12 text-muted-foreground">
              No results yet
            </TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </Card>
  )
}
