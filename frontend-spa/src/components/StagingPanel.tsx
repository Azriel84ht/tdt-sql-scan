import React from 'react';

interface StagingPanelProps {
  files: File[];
  onRemoveFile: (file: File) => void;
}

const StagingPanel: React.FC<StagingPanelProps> = ({ files, onRemoveFile }) => {
  return (
    <div style={panelStyles}>
      <h4>Staging Area</h4>
      {files.length === 0 ? (
        <p>No files uploaded.</p>
      ) : (
        <ul style={listStyles}>
          {files.map((file, index) => (
            <li key={index} style={listItemStyles}>
              <span>{file.name}</span>
              <button onClick={() => onRemoveFile(file)} style={buttonStyles}>Remove</button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

const panelStyles: React.CSSProperties = {
  marginTop: '20px',
  padding: '10px',
  border: '1px solid #ddd',
  borderRadius: '4px',
};

const listStyles: React.CSSProperties = {
  listStyleType: 'none',
  padding: 0,
};

const listItemStyles: React.CSSProperties = {
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'center',
  padding: '5px 0',
};

const buttonStyles: React.CSSProperties = {
  marginLeft: '10px',
};

export default StagingPanel;
