import React from 'react';

interface StagingPanelProps {
  files: File[];
  onRemoveFile: (file: File) => void;
  onAnalyze: () => void;
}

const StagingPanel: React.FC<StagingPanelProps> = ({ files, onRemoveFile, onAnalyze }) => {
  return (
    <div className="bg-gray-700 rounded-lg p-4">
      <h3 className="text-lg font-semibold text-white mb-4">Staged Files</h3>
      <div className="space-y-2">
        {files.length === 0 ? (
          <p className="text-gray-400 text-sm">No files uploaded yet.</p>
        ) : (
          files.map((file, index) => (
            <div key={index} className="flex items-center justify-between bg-gray-600 p-2 rounded-md">
              <div className="flex items-center space-x-2">
                <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z"></path></svg>
                <span className="text-sm text-white truncate">{file.name}</span>
              </div>
              <button
                onClick={() => onRemoveFile(file)}
                className="text-gray-400 hover:text-white transition-colors duration-200"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12"></path></svg>
              </button>
            </div>
          ))
        )}
      </div>
      <button
        onClick={onAnalyze}
        disabled={files.length === 0}
        className="mt-4 w-full flex justify-center py-3 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-gradient-to-r from-green-500 to-teal-500 hover:from-green-600 hover:to-teal-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-teal-500 disabled:opacity-50 transition-all duration-300 ease-in-out"
      >
        Analyze
      </button>
    </div>
  );
};

export default StagingPanel;
