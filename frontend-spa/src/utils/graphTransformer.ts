import type { Edge, Node } from 'reactflow';

// Assuming ParseResultDto and StatementDto types are defined elsewhere,
// for example in an `api-client` or `types` module.
// For now, we'll use simplified interfaces.

interface StatementDto {
  commandName?: string;
  rawContent: string;
  // other properties...
}

interface ParseResultDto {
  statements: StatementDto[];
  // other properties...
}

export const transformToGraph = (data: ParseResultDto) => {
  const nodes: Node[] = [];
  const edges: Edge[] = [];

  data.statements.forEach((statement, index) => {
    const nodeId = `node-${index}`;
    nodes.push({
      id: nodeId,
      data: { label: statement.commandName || statement.rawContent.substring(0, 50) },
      position: { x: 150, y: index * 100 }, // Simple vertical layout
    });

    if (index > 0) {
      const previousNodeId = `node-${index - 1}`;
      edges.push({
        id: `edge-${index - 1}-${index}`,
        source: previousNodeId,
        target: nodeId,
        type: 'smoothstep',
      });
    }
  });

  return { nodes, edges };
};
