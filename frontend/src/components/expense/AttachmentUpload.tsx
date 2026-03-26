import React, { useRef, useState } from 'react';
import { Upload, Trash2, FileText, Image } from 'lucide-react';
import { Attachment } from '../../types';
import { formatFileSize } from '../../utils/formatters';
import expenseService from '../../services/expenseService';
import toast from 'react-hot-toast';

interface Props {
  expenseId: number;
  attachments: Attachment[];
  readonly?: boolean;
  onUpdate: (attachments: Attachment[]) => void;
}

const AttachmentUpload: React.FC<Props> = ({
  expenseId, attachments, readonly = false, onUpdate
}) => {
  const inputRef = useRef<HTMLInputElement>(null);
  const [uploading, setUploading] = useState(false);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (file.size > 10 * 1024 * 1024) {
      toast.error('File size must not exceed 10MB');
      return;
    }
    const allowed = ['image/jpeg', 'image/png', 'application/pdf'];
    if (!allowed.includes(file.type)) {
      toast.error('Only JPG, PNG and PDF files are allowed');
      return;
    }

    setUploading(true);
    try {
      const response = await expenseService.uploadAttachment(expenseId, file);
      onUpdate(response.data.attachments);
      toast.success('File uploaded successfully');
    } catch {
      toast.error('Failed to upload file');
    } finally {
      setUploading(false);
      if (inputRef.current) inputRef.current.value = '';
    }
  };

  const handleDelete = async (attachmentId: number) => {
    try {
      await expenseService.deleteAttachment(expenseId, attachmentId);
      onUpdate(attachments.filter(a => a.id !== attachmentId));
      toast.success('Attachment removed');
    } catch {
      toast.error('Failed to remove attachment');
    }
  };

  const getIcon = (fileType: string) =>
    fileType.startsWith('image/') ? <Image className="h-4 w-4" /> : <FileText className="h-4 w-4" />;

  return (
    <div>
      {!readonly && (
        <div
          className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center cursor-pointer hover:border-primary-400 transition-colors"
          onClick={() => inputRef.current?.click()}
        >
          <input
            ref={inputRef}
            type="file"
            accept=".jpg,.jpeg,.png,.pdf"
            className="hidden"
            onChange={handleFileChange}
          />
          <Upload className="h-8 w-8 text-gray-400 mx-auto mb-2" />
          <p className="text-sm text-gray-600">
            {uploading ? 'Uploading...' : 'Click to upload or drag and drop'}
          </p>
          <p className="text-xs text-gray-400 mt-1">JPG, PNG, PDF up to 10MB</p>
        </div>
      )}

      {attachments.length > 0 && (
        <ul className="mt-3 space-y-2">
          {attachments.map(att => (
            <li key={att.id}
                className="flex items-center justify-between p-3 bg-gray-50 rounded-md border">
              <div className="flex items-center gap-3">
                <span className="text-gray-500">{getIcon(att.fileType)}</span>
                <div>
                  <a
                    href={att.downloadUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="text-sm font-medium text-primary-600 hover:underline truncate max-w-xs block"
                  >
                    {att.fileName}
                  </a>
                  <p className="text-xs text-gray-400">{formatFileSize(att.fileSize)}</p>
                </div>
              </div>
              {!readonly && (
                <button
                  onClick={() => handleDelete(att.id)}
                  className="text-red-500 hover:text-red-700 p-1"
                  title="Remove attachment"
                >
                  <Trash2 className="h-4 w-4" />
                </button>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default AttachmentUpload;
