import React from 'react';

interface FileIconProps {
  filename: string;
}

const FileIcon: React.FC<FileIconProps> = ({ filename }) => {
  const getFileExtension = (name: string) => {
    return name.slice(((name.lastIndexOf(".") - 1) >>> 0) + 2);
  };

  const extension = getFileExtension(filename);

  if (extension === 'sql') {
    return (
      <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 7v10m16-10v10M4 12h16m-16-5h16m-16 10h16"></path>
      </svg>
    );
  }

  return (
    <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z"></path>
    </svg>
  );
};

export default FileIcon;
