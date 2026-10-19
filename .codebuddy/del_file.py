import os
import shutil

def del_deepest(path):
    """找到最深处空目录删除"""
    while True:
        try:
            contents = os.listdir(path)
        except:
            break
        if not contents:
            os.rmdir(path)
            return
        path = os.path.join(path, contents[0])

# 开始
del_deepest(r"\\?\D:\idea2025\project\systemCookies\.codebuddy\skills")

# 删完后删整个 skills
shutil.rmtree(r"\\?\D:\idea2025\project\systemCookies\.codebuddy\skills", ignore_errors=True)
print("✅ 完成！")