import React, { useCallback } from 'react';
import { useDropzone } from 'react-dropzone';

interface FileUploadAreaProps {
  onFilesUpload: (files: File[]) => void;
}

const FileUploadArea: React.FC<FileUploadAreaProps> = ({ onFilesUpload }) => {
  const onDrop = useCallback((acceptedFiles: File[]) => {
    onFilesUpload(acceptedFiles);
  }, [onFilesUpload]);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({ onDrop });

  const baseClasses = "border-2 border-dashed rounded-lg p-10 text-center cursor-pointer transition-colors duration-300 ease-in-out";
  const inactiveClasses = "border-gray-600 hover:border-indigo-500";
  const activeClasses = "border-indigo-500 bg-gray-700";

  return (
    <div
      {...getRootProps()}
      className={`${baseClasses} ${isDragActive ? activeClasses : inactiveClasses}`}
    >
      <input {...getInputProps()} />
      <div className="flex flex-col items-center justify-center space-y-4">
        <svg className="w-12 h-12 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"></path></svg>
        {isDragActive ? (
          <p className="text-lg font-semibold text-white">Drop the files here...</p>
        ) : (
          <div>
            <p className="text-lg font-semibold text-white">Drag & drop files or click to upload</p>
            <p className="text-sm text-gray-400">Select the files you want to analyze</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default FileUploadArea;
