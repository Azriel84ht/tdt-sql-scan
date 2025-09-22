import React, { useEffect } from 'react';
import ReactFlow, { useNodesState, useEdgesState, Controls, MiniMap, Background } from 'reactflow';
import 'reactflow/dist/style.css';
import { transformToGraph } from '../utils/graphTransformer';

// Simplified DTOs for props
interface StatementDto {
  commandName?: string;
  rawContent: string;
}

interface ParseResultDto {
  statements: StatementDto[];
}

interface GraphVisualizationPanelProps {
  analysisResult: ParseResultDto;
}

const GraphVisualizationPanel: React.FC<GraphVisualizationPanelProps> = ({ analysisResult }) => {
  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);

  useEffect(() => {
    if (analysisResult) {
      const { nodes: newNodes, edges: newEdges } = transformToGraph(analysisResult);
      setNodes(newNodes);
      setEdges(newEdges);
    }
  }, [analysisResult, setNodes, setEdges]);

  return (
    <div style={{ width: '100%', height: '500px', border: '1px solid #ccc' }}>
      <ReactFlow
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        fitView
      >
        <Controls />
        <MiniMap />
        <Background />
      </ReactFlow>
    </div>
  );
};

export default GraphVisualizationPanel;
