import React, { useState } from 'react';
import { useAuthStore } from '../store/authStore';
import FileUploadArea from '../components/FileUploadArea';
import StagingPanel from '../components/StagingPanel';
import apiClient from '../api/apiClient';

interface AnalysisResult {
  // Using a more specific type than any
  [key: string]: unknown;
}

const HomePage: React.FC = () => {
  const { logout } = useAuthStore();
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
    setIsLoading(true);
    setAnalysisResults([]);

    const fileReadPromises = stagedFiles.map(file => {
      return new Promise<string>((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = (event) => {
          resolve(event.target?.result as string);
        };
        reader.onerror = (error) => {
          reject(error);
        };
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
      // Optionally, set an error state to display to the user
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div>
      <h1>Home Page</h1>
      <p>Welcome! You are logged in.</p>
      <button onClick={logout}>Logout</button>

      <FileUploadArea onFilesUpload={handleFilesUpload} />
      <StagingPanel files={stagedFiles} onRemoveFile={handleRemoveFile} />

      <button onClick={handleAnalyze} disabled={stagedFiles.length === 0 || isLoading}>
        {isLoading ? 'Analyzing...' : 'Analyze'}
      </button>

      {analysisResults.length > 0 && (
        <div>
          <h3>Analysis Results</h3>
          <pre>{JSON.stringify(analysisResults, null, 2)}</pre>
        </div>
      )}
    </div>
  );
};

export default HomePage;
