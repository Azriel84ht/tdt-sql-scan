import React from 'react';
import Spinner from './common/Spinner';
import FileIcon from './common/FileIcon';

interface StagingPanelProps {
  files: File[];
  onRemoveFile: (file: File) => void;
  onAnalyze: () => void;
  isLoading: boolean;
}

const StagingPanel: React.FC<StagingPanelProps> = ({ files, onRemoveFile, onAnalyze, isLoading }) => {
  return (
    <div className="bg-gray-700 rounded-lg p-4">
      <h3 className="text-lg font-semibold text-white mb-4">Staged Files</h3>
      <div className="space-y-2">
        {files.length === 0 ? (
          <p className="text-gray-400 text-sm">No files uploaded yet.</p>
        ) : (
          files.map((file, index) => (
            <div key={index} className="flex items-center justify-between bg-gray-600 p-2 rounded-md">
              <div className="flex items-center space-x-2 min-w-0">
                <FileIcon filename={file.name} />
                <span className="text-sm text-white truncate">{file.name}</span>
              </div>
              <button
                onClick={() => onRemoveFile(file)}
                className="text-gray-400 hover:text-red-500 transition-colors duration-200"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
              </button>
            </div>
          ))
        )}
      </div>
      <button
        onClick={onAnalyze}
        disabled={files.length === 0 || isLoading}
        className="mt-4 w-full flex items-center justify-center py-3 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-gradient-to-r from-green-500 to-teal-500 hover:from-green-600 hover:to-teal-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-teal-500 disabled:opacity-50 transition-all duration-300 ease-in-out"
      >
        {isLoading ? <Spinner /> : 'Analyze'}
      </button>
    </div>
  );
};

export default StagingPanel;
