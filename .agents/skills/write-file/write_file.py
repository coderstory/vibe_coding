#!/usr/bin/env python3
import sys
import json
import os
from pathlib import Path

def write_file(file_path: str, content: str, create_dirs: bool = False) -> dict:
    try:
        path = Path(file_path)
        
        if create_dirs:
            path.parent.mkdir(parents=True, exist_ok=True)
        
        path.write_text(content, encoding='utf-8')
        
        return {
            "success": True,
            "file": str(path.absolute()),
            "bytes": path.stat().st_size
        }
    except Exception as e:
        return {
            "success": False,
            "error": str(e)
        }

def main():
    if len(sys.argv) < 3:
        print(json.dumps({
            "success": False,
            "error": "Usage: write_file.py <file_path> <content> [--create-dirs]"
        }))
        sys.exit(1)
    
    file_path = sys.argv[1]
    content = sys.argv[2]
    create_dirs = "--create-dirs" in sys.argv
    
    result = write_file(file_path, content, create_dirs)
    print(json.dumps(result))
    
    sys.exit(0 if result["success"] else 1)

if __name__ == "__main__":
    main()
