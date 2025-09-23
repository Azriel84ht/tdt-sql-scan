import React, { useState } from 'react';
import { toast } from 'react-hot-toast';
import apiClient from '../api/apiClient';
import FileUploadArea from '../components/FileUploadArea';
import StagingPanel from '../components/StagingPanel';
import GraphVisualizationPanel from '../components/GraphVisualizationPanel';
import Spinner from '../components/common/Spinner';

interface StatementDto {
  commandName?: string;
  rawContent: string;
}

interface AnalysisResult {
  statements: StatementDto[];
  [key: string]: unknown;
}

const HomePage: React.FC = () => {
  const [stagedFiles, setStagedFiles] = useState<File[]>([]);
  const [analysisResults, setAnalysisResults] = useState<AnalysisResult[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  const handleFilesUpload = (files: File[]) => {
    setStagedFiles(prevFiles => [...prevFiles, ...files]);
  };

  const handleRemoveFile = (fileToRemove: File) => {
    setStagedFiles(prevFiles => prevFiles.filter(file => file !== fileToRemove));
  };

  const handleAnalyze = async () => {
    if (stagedFiles.length === 0) return;

    setIsLoading(true);
    setAnalysisResults([]);

    const fileReadPromises = stagedFiles.map(file => {
      return new Promise<string>((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = (event) => resolve(event.target?.result as string);
        reader.onerror = (error) => reject(error);
        reader.readAsText(file);
      });
    });

    try {
      const fileContents = await Promise.all(fileReadPromises);
      const analysisPromises = fileContents.map(content =>
        apiClient.post('/api/v1/parse/poc', { script_content: content })
      );
      const responses = await Promise.all(analysisPromises);
      setAnalysisResults(responses.map(res => res.data));
    } catch (error) {
      console.error('Error during analysis:', error);
      toast.error('An error occurred during analysis. Please check the console for details.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="grid md:grid-cols-3 gap-4 h-full">
      <div className="md:col-span-1 space-y-4">
        <FileUploadArea onFilesUpload={handleFilesUpload} />
        <StagingPanel
          files={stagedFiles}
          onRemoveFile={handleRemoveFile}
          onAnalyze={handleAnalyze}
          isLoading={isLoading}
        />
      </div>
      <div className="md:col-span-2 relative">
        {isLoading && (
          <div className="absolute inset-0 bg-gray-900 bg-opacity-75 flex flex-col items-center justify-center z-10 rounded-lg">
            <Spinner className="w-16 h-16 text-white" />
            <p className="text-white text-2xl mt-4">Analyzing...</p>
          </div>
        )}
        {analysisResults.length > 0 ? (
          <div className="space-y-4">
            {analysisResults.map((result, index) => (
              <GraphVisualizationPanel key={index} analysisResult={result} />
            ))}
          </div>
        ) : (
          !isLoading && (
            <div className="flex items-center justify-center h-full bg-gray-800 rounded-lg">
              <p className="text-gray-400 text-2xl text-center p-4">
                Upload files and click Analyze to see the results.
              </p>
            </div>
          )
        )}
      </div>
    </div>
  );
};

export default HomePage;
