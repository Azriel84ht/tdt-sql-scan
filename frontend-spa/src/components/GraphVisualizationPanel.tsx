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
    <div className="bg-gray-800 rounded-lg shadow-lg p-4 h-[600px]">
      <h3 className="text-lg font-semibold text-white mb-4">Query Plan</h3>
      <div className="w-full h-full rounded-md overflow-hidden">
        <ReactFlow
          nodes={nodes}
          edges={edges}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          fitView
          className="bg-gray-700"
        >
          <Controls />
          <MiniMap nodeColor="#6366f1" />
          <Background color="#4b5563" gap={16} />
        </ReactFlow>
      </div>
    </div>
  );
};

export default GraphVisualizationPanel;
