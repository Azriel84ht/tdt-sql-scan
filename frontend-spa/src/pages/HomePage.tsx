import React from 'react';
import { useAnalysisStore } from '../store/analysisStore';
import GraphVisualizationPanel from '../components/GraphVisualizationPanel';

const HomePage: React.FC = () => {
  const { analysisResults, isLoading } = useAnalysisStore();

  return (
    <div className="h-full">
      {isLoading ? (
        <div className="flex items-center justify-center h-full">
          <p className="text-white text-2xl">Analyzing...</p>
        </div>
      ) : analysisResults.length > 0 ? (
        <div className="space-y-4">
          {analysisResults.map((result, index) => (
            <GraphVisualizationPanel key={index} analysisResult={result} />
          ))}
        </div>
      ) : (
        <div className="flex items-center justify-center h-full">
          <p className="text-gray-400 text-2xl">Upload files and click Analyze to see the results.</p>
        </div>
      )}
    </div>
  );
};

export default HomePage;
