import React from 'react';
import { Outlet } from 'react-router-dom';
import FileUploadArea from '../FileUploadArea';
import StagingPanel from '../StagingPanel';
import { useAnalysisStore } from '../../store/analysisStore';

const MainLayout: React.FC = () => {
  const { stagedFiles, addFiles, removeFile, analyzeFiles } = useAnalysisStore();

  return (
    <div className="md:flex h-screen bg-gray-900 text-white">
      {/* Sidebar */}
      <div className="w-full md:w-96 bg-gray-800 flex-shrink-0 p-4 space-y-4 flex flex-col">
        <h1 className="text-2xl font-bold text-center">Data Analyzer</h1>
        <div className="space-y-6 flex-grow flex flex-col">
          <FileUploadArea onFilesUpload={addFiles} />
          <div className="flex-grow">
            <StagingPanel files={stagedFiles} onRemoveFile={removeFile} onAnalyze={analyzeFiles} />
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Header */}
        <header className="bg-gray-800 shadow-md p-4">
          <h2 className="text-xl font-semibold">Graph Visualization</h2>
        </header>

        {/* Content Area */}
        <main className="flex-1 overflow-x-hidden overflow-y-auto bg-gray-900 p-4">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default MainLayout;
