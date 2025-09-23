import { create } from 'zustand';
import apiClient from '../api/apiClient';

interface StatementDto {
  commandName?: string;
  rawContent: string;
}

interface AnalysisResult {
  statements: StatementDto[];
  [key: string]: unknown;
}

interface AnalysisState {
  stagedFiles: File[];
  analysisResults: AnalysisResult[];
  isLoading: boolean;
  addFiles: (files: File[]) => void;
  removeFile: (fileToRemove: File) => void;
  analyzeFiles: () => Promise<void>;
}

export const useAnalysisStore = create<AnalysisState>((set, get) => ({
  stagedFiles: [],
  analysisResults: [],
  isLoading: false,
  addFiles: (files) => {
    set(state => ({ stagedFiles: [...state.stagedFiles, ...files] }));
  },
  removeFile: (fileToRemove) => {
    set(state => ({
      stagedFiles: state.stagedFiles.filter(file => file !== fileToRemove),
    }));
  },
  analyzeFiles: async () => {
    const { stagedFiles } = get();
    if (stagedFiles.length === 0) return;

    set({ isLoading: true, analysisResults: [] });

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
      set({ analysisResults: responses.map(res => res.data) });
    } catch (error) {
      console.error('Error during analysis:', error);
      // Optionally, set an error state to display to the user
    } finally {
      set({ isLoading: false });
    }
  },
}));
