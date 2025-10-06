import os
from collections import OrderedDict
import copy

import numpy
import output_bdiff_html
import shutil
from pprint import pprint
from shutil import copyfile
import pymysql
from numpy.core.shape_base import block
import Myers
import time
import subprocess
import re
import BDiffgit
import BDiff
# import output_bdiff_html
import psutil
import requests
from bs4 import BeautifulSoup
import random
import numpy as np
import string


conn = pymysql.connect(host="localhost", user="root", password="***", database="bdiff")
cursor = conn.cursor()

def export_git_samples(sample_file, output_path_prefix, path_prefix):
    infile = open(sample_file, 'r')
    lines = infile.readlines()[1:]
    i = 0
    for line in lines:
        fields = line.split(',')
        print(i, fields[3], fields[4], fields[5])
        left_path = path_prefix + "\\before\\" + fields[3] + "\\" + fields[4] + "\\" + fields[5]
        right_path = path_prefix + "\\after\\" + fields[3] + "\\" + fields[4] + "\\" + fields[5]
        output_path = output_path_prefix + "\\" + fields[3] + "-" + fields[4] + "-" + fields[5] + ".html"
        # The BDiff algorithm is configured to recognize Edit Actions (EA) as only deleted lines and added lines.
        # The default algorithm for computing these differences is set to Myers' algorithm.
        output_bdiff_html.run(left_path, right_path, output_path)
        i += 1

def export_bdiff_samples(sample_file, output_path_prefix, path_prefix):
    infile = open(sample_file, 'r')
    lines = infile.readlines()[1:]
    i = 0
    for line in lines:
        fields = line.split(',')
        print(i, fields[3], fields[4], fields[5])
        left_path = path_prefix + "\\before\\" + fields[3] + "\\" + fields[4] + "\\" + fields[5]
        right_path = path_prefix + "\\after\\" + fields[3] + "\\" + fields[4] + "\\" + fields[5]
        output_path = output_path_prefix + "\\" + fields[3] + "-" + fields[4] + "-" + fields[5] + ".html"
        output_bdiff_html.run(left_path, right_path, output_path)
        i += 1

def export_ldiff_samples_in_html(sample_file, output_path_prefix, path_prefix):
    infile = open(sample_file, 'r')
    lines = infile.readlines()[1:]
    i = 0
    for line in lines:
        fields = line.split(',')
        print(i, fields[3], fields[4], fields[5])
        left_path = path_prefix + "\\before\\" + fields[3] + "\\" + fields[4] + "\\" + fields[5]
        right_path = path_prefix + "\\after\\" + fields[3] + "\\" + fields[4] + "\\" + fields[5]
        output_path = output_path_prefix + "\\" + "ldiff-" + fields[3] + "-" + fields[4] + "-" + fields[5] + ".html"
        outfile = open(output_path, 'w')
        result = subprocess.run("perl ldiff.pl -o ext %s %s" % (left_path, right_path), stdout=subprocess.PIPE,
                                 encoding='utf-8')
        edit_scripts =[]
        src_line_no = 1
        dest_line_no = 1
        ldiff_edits = [res.split(":")[0] for res in str(result.stdout).splitlines()]
        # 调整删除和新增顺序
        for ind in range(1, len(ldiff_edits[:])):
            ldiff_edit = ldiff_edits[ind]
            last_ldiff_edit = ldiff_edits[ind-1]
            if 'd' in ldiff_edit and 'a' in last_ldiff_edit and int(ldiff_edit.split(",")[0]) < int(last_ldiff_edit.split(",")[-1]):
                tmp = ldiff_edits[ind]
                ldiff_edits[ind] = last_ldiff_edit
                ldiff_edits[ind - 1] = tmp
        for edit in ldiff_edits:
            if 'a' in edit:
                start, end = edit.replace('a', ',').split(',')[2:]
                src_ = int(edit.split(',')[0]) + 1
                # src_line_no = int(edit.split(',')[0])
                for dest_no in range(int(start), int(end) + 1):
                    edit_scripts.append({"mode": "insert", "dest_line": dest_no, "src_line": src_,
                                 "edit_action": "Insert " + str(dest_no)})
                    dest_line_no += 1
            elif 'd' in edit:
                start, end = edit.replace('d', ',').split(',')[:2]
                dest_ = int(edit.split(',')[-1]) + 1
                for src_no in range(int(start), int(end) + 1):
                    edit_scripts.append({"mode": "delete", "dest_line": dest_, "src_line": src_no,
                                         "edit_action": "Delete " + str(src_no)})
                    src_line_no += 1
            elif 'c' in edit:
                start, temp, end = edit.split(',')
                edit_scripts.append({"mode": "move", "dest_line": int(end), "src_line": int(start),
                                     "edit_action": "Change " + start + " to " + end, "block_length": 1, "updates": []})
                src_line_no += 1
                dest_line_no += 1
        edit_scripts.sort(key=lambda x: (x["src_line"], x["dest_line"]))
        for esr in edit_scripts:
            if esr['mode'] == 'delete':
                del esr['dest_line']
            elif esr['mode'] == 'insert':
                del esr['src_line']
        src_infile = open(left_path, 'r', encoding='utf-8')
        dest_infile = open(right_path, 'r', encoding='utf-8')
        src_lines_list = src_infile.read().splitlines()
        dest_lines_list = dest_infile.read().splitlines()
        html_content = output_bdiff_html.get_html_content(left_path, right_path, src_lines_list, dest_lines_list, edit_scripts)
        output_bdiff_html.save_file(output_path, html_content)
        outfile.close()

def kill_ps_by_psname(psname):
    for proc in psutil.process_iter(['pid', 'name']):
        if proc.info['name'] == psname:
            proc.kill()


def convert_line_endings(file_path, old_ending=b'\n', new_ending=b'\r\n'):
    try:
        with open(file_path, 'rb') as f:
            content = f.read()
        new_content = content.replace(old_ending, new_ending)
        with open(file_path, 'wb') as f:
            f.write(new_content)
        print(f"File '{file_path}' 's line break characters have been successfully converted to {new_ending}")
    except FileNotFoundError:
        print(f"Error: File '{file_path}' not found.")
    except Exception as e:
        print(f"Processing File '{file_path}' encounters error：{e}")



def export_gumtree_samples(sample_file, output_path_prefix, path_prefix):
    infile = open(sample_file, 'r')
    lines = infile.readlines()[1:]
    i = 0
    gt_url = "http://localhost:4567/vanilla-diff/0"
    for line in lines:
        fields = line.split(',')
        print(i, fields[3], fields[4], fields[5])
        left_path = path_prefix + "\\before\\" + fields[3] + "\\" + fields[4] + "\\" + fields[5]
        right_path = path_prefix + "\\after\\" + fields[3] + "\\" + fields[4] + "\\" + fields[5]
        output_path = output_path_prefix + "\\" + fields[3] + "-" + fields[4] + "-" + fields[5] + "-gumtree.html"
        convert_line_endings(left_path)
        convert_line_endings(right_path)
        outfile = open(output_path, 'w', encoding='utf-8')
        process = subprocess.Popen("java -jar gumtree.jar webdiff %s %s" % (left_path, right_path), shell=True,
                                   stdout=subprocess.PIPE)
        response = requests.get(gt_url)
        if response.status_code == 200:
            # html_content = response.text
            # outfile.write(html_content)
            soup = BeautifulSoup(response.text, 'html.parser')

            style_1 = soup.new_tag('style')
            script_1 = soup.new_tag('script')
            script_2 = soup.new_tag('script')

            with open(r'html\gumtree\vanilla.css') as cssFile:
                style_1.string = cssFile.read()

            with open(r'html\gumtree\shortcuts.js') as scriptFile1:
                script_1.string = scriptFile1.read()

            with open(r'html\gumtree\vanilla.js') as scriptFile2:
                script_2.string = scriptFile2.read()

            soup.head.append(style_1)
            soup.head.append(script_1)
            soup.head.append(script_2)

            outfile.write(soup.prettify())

            outfile.close()
        else:
            print("Failed to retrieve the webpage")
        kill_ps_by_psname("java.exe")




def run_gumtree(projs_path, lang):
    cursor.execute("select Id, Name from Projects where Language = '%s'" % (lang))
    projs = cursor.fetchall()
    error_num = 0
    for proj in projs:
        before_src = projs_path + "\\before\\" + proj[1]
        proj_commits = os.listdir(before_src)
        for proj_commit in proj_commits:
            before_files = os.listdir(before_src + "\\" + proj_commit)
            for before_file in before_files:
                print(proj, proj_commit, before_file)
                src_path = before_src + "\\" + proj_commit + "\\" + before_file
                dest_path = projs_path + "\\after\\" + proj[1] + "\\" + proj_commit + "\\" + before_file
                start_time = time.perf_counter()
                result = subprocess.run(
                    "java -jar gumtree.jar textdiff -f XML %s %s" % (src_path, dest_path), stdout=subprocess.PIPE,
                    encoding='utf-8', text=True)
                end_time = time.perf_counter()
                run_time = end_time - start_time
                if result.returncode != 0:
                    error_num += 1
                editaction_list = re.findall(r'<actions>\n(.*)\n</actions>', result.stdout, re.S)
                if editaction_list:
                    edit_scripts = []
                    ins_num, del_num, upd_num, mov_num = 0, 0, 0, 0
                    for line in editaction_list[0].splitlines():
                        edit_script = line.strip().replace("'", "").replace('"', "")
                        if edit_script.startswith('<insert'):
                            ins_num += 1
                            edit_scripts.append(edit_script)
                        elif edit_script.startswith('<delete'):
                            del_num += 1
                            edit_scripts.append(edit_script)
                        elif edit_script.startswith('<update'):
                            upd_num += 1
                            edit_scripts.append(edit_script)
                        elif edit_script.startswith('<move'):
                            mov_num += 1
                            edit_scripts.append(edit_script)
                    try:
                        src_infile = open(src_path, 'r', encoding='utf-8')
                        dest_infile = open(dest_path, 'r', encoding='utf-8')
                    except:
                        print("File read error")
                        continue
                    src_lines_list = src_infile.readlines()
                    dest_lines_list = dest_infile.readlines()
                    sql = '''Insert into GumTreeEditScripts (ProjId, ProjName, CommitSha, FileName, FileLengthBefore, FileLengthAfter, nEdits, nDeletes, nInserts, nMoves, nUpdates, EditScript, RunTime) \
                                                                            values ('%s', '%s', '%s', '%s', %d, %d, %d, %d, %d , %d, %d, "%s", %s)''' \
                          % (proj[0], proj[1], proj_commit, before_file, len(src_lines_list), len(dest_lines_list), \
                             len(edit_scripts), del_num, ins_num, mov_num, upd_num, str(edit_scripts), str(run_time))
                    cursor.execute(sql)
            conn.commit()
    print(error_num)

def run_gumtree_runtime(projs_path, lang, i):
    cursor.execute("select Id, Name from Projects where Language = '%s'" % (lang))
    projs = cursor.fetchall()
    error_num = 0
    for proj in projs:
        before_src = projs_path + "\\before\\" + proj[1]
        proj_commits = os.listdir(before_src)
        for proj_commit in proj_commits:
            before_files = os.listdir(before_src + "\\" + proj_commit)
            for before_file in before_files:
                cursor.execute("select Id from gumtreeeditScripts where ProjId = '%s' and ProjName = '%s' and CommitSha = '%s' and FileName = '%s' and Language = '%s' and DiffId is not NULL;" % (proj[0], proj[1], proj_commit, before_file, lang))
                analysis_record = cursor.fetchone()
                if not analysis_record:
                    continue
                print(proj, proj_commit, before_file)
                src_path = before_src + "\\" + proj_commit + "\\" + before_file
                dest_path = projs_path + "\\after\\" + proj[1] + "\\" + proj_commit + "\\" + before_file
                start_time = time.perf_counter()
                result = subprocess.run(
                    "java -jar gumtree.jar textdiff -f XML %s %s" % (src_path, dest_path), stdout=subprocess.PIPE,
                    encoding='utf-8', text=True)
                end_time = time.perf_counter()
                run_time = end_time - start_time
                if result.returncode != 0:
                    error_num += 1
                editaction_list = re.findall(r'<actions>\n(.*)\n</actions>', result.stdout, re.S)
                if editaction_list:
                    edit_scripts = []
                    ins_num, del_num, upd_num, mov_num = 0, 0, 0, 0
                    for line in editaction_list[0].splitlines():
                        edit_script = line.strip().replace("'", "").replace('"', "")
                        if edit_script.startswith('<insert'):
                            ins_num += 1
                            edit_scripts.append(edit_script)
                        elif edit_script.startswith('<delete'):
                            del_num += 1
                            edit_scripts.append(edit_script)
                        elif edit_script.startswith('<update'):
                            upd_num += 1
                            edit_scripts.append(edit_script)
                        elif edit_script.startswith('<move'):
                            mov_num += 1
                            edit_scripts.append(edit_script)
                    try:
                        src_infile = open(src_path, 'r', encoding='utf-8')
                        dest_infile = open(dest_path, 'r', encoding='utf-8')
                    except:
                        print("File read error")
                        continue
                    sql = "update GumTreeEditScripts set RunTime%s = '%s' where ProjId = '%s' and ProjName = '%s' and CommitSha = '%s' and FileName = '%s' and Language = '%s';" % (
                        str(i), run_time, proj[0], proj[1], proj_commit, before_file, lang)
                    cursor.execute(sql)
            conn.commit()
    print(error_num)


def run_ldiff(projs_path, lang):
    cursor.execute("select Id, Name from Projects where Language = '%s'" % (lang))
    projs = cursor.fetchall()
    for proj in projs:
        before_src = projs_path + "\\before\\" + proj[1]
        proj_commits = os.listdir(before_src)
        for proj_commit in proj_commits:
            before_files = os.listdir(before_src + "\\" + proj_commit)
            for before_file in before_files:
                print(proj, proj_commit, before_file)
                src_path = before_src + "\\" + proj_commit + "\\" + before_file
                dest_path = projs_path + "\\after\\" + proj[1] + "\\" + proj_commit + "\\" + before_file
                start_time = time.perf_counter()
                result = subprocess.run(
                    "perl ldiff.pl -o ext %s %s" % (src_path, dest_path), stdout=subprocess.PIPE, encoding='utf-8',
                    text=True)
                end_time = time.perf_counter()
                run_time = end_time - start_time
                edit_scripts = []
                ins_num, del_num, upd_num = 0, 0, 0
                for line in str(result.stdout).splitlines():
                    edit = line.split(":")[0]
                    if 'a' in edit:
                        start, end = edit.replace('a', ',').split(',')[2:]
                        ins_num += (int(end) - int(start) + 1)
                        edit_scripts.append(edit)
                    elif 'd' in edit:
                        start, end = edit.replace('d', ',').split(',')[:2]
                        del_num += (int(end) - int(start) + 1)
                        edit_scripts.append(edit)
                    elif 'c' in edit:
                        upd_num += 1
                        edit_scripts.append(edit)
                try:
                    src_infile = open(src_path, 'r', encoding='utf-8')
                    dest_infile = open(dest_path, 'r', encoding='utf-8')
                except:
                    print("File read error")
                    continue
                src_lines_list = src_infile.readlines()
                dest_lines_list = dest_infile.readlines()
                sql = '''Insert into ldiffEditScripts (ProjId, ProjName, CommitSha, FileName, FileLengthBefore, FileLengthAfter, nInserts, nDeletes, nUpdates, nEdits, EditScript, RunTime) \
                                                        values ('%s', '%s', '%s', '%s', %d, %d, %d, %d, %d ,%d, "%s", %s)''' \
                      % (proj[0], proj[1], proj_commit, before_file, len(src_lines_list), len(dest_lines_list),
                         ins_num, del_num, upd_num, ins_num + del_num + upd_num, str(edit_scripts), str(run_time))
                cursor.execute(sql)
            conn.commit()

def run_ldiff_runtime(projs_path, lang, i):
    cursor.execute("select Id, Name from Projects where Language = '%s'" % (lang))
    projs = cursor.fetchall()
    for proj in projs:
        before_src = projs_path + "\\before\\" + proj[1]
        proj_commits = os.listdir(before_src)
        for proj_commit in proj_commits:
            before_files = os.listdir(before_src + "\\" + proj_commit)
            for before_file in before_files:
                cursor.execute("select Id from ldiffeditscripts where ProjId = '%s' and ProjName = '%s' and CommitSha = '%s' and FileName = '%s' and Language = '%s' and DiffId is not NULL;" % (proj[0], proj[1], proj_commit, before_file, lang))
                analysis_record = cursor.fetchone()
                if not analysis_record:
                    continue
                print(proj, proj_commit, before_file)
                src_path = before_src + "\\" + proj_commit + "\\" + before_file
                dest_path = projs_path + "\\after\\" + proj[1] + "\\" + proj_commit + "\\" + before_file
                start_time = time.perf_counter()
                result = subprocess.run(
                    "perl ldiff.pl -o ext %s %s" % (src_path, dest_path), stdout=subprocess.PIPE, encoding='utf-8',
                    text=True)
                end_time = time.perf_counter()
                run_time = end_time - start_time
                sql = "update ldiffeditscripts set RunTime%s = '%s' where ProjId = '%s' and ProjName = '%s' and CommitSha = '%s' and FileName = '%s' and Language = '%s';" % (
                    str(i), run_time, proj[0], proj[1], proj_commit, before_file, lang)
                # sql = '''Insert into ldiffEditScripts (ProjId, ProjName, CommitSha, FileName, FileLengthBefore, FileLengthAfter, nInserts, nDeletes, nUpdates, nEdits, EditScript, RunTime) \
                #                                         values ('%s', '%s', '%s', '%s', %d, %d, %d, %d, %d ,%d, "%s", %s)''' \
                #       % (proj[0], proj[1], proj_commit, before_file, len(src_lines_list), len(dest_lines_list),
                #          ins_num, del_num, upd_num, ins_num + del_num + upd_num, str(edit_scripts), str(run_time))
                cursor.execute(sql)
            conn.commit()


def run_git_Myers(projs_path, lang):
    cursor.execute("select Id, Name from Projects where Language = '%s'" % (lang))
    projs = cursor.fetchall()
    for proj in projs:
        before_src = projs_path + "\\before\\" + proj[1]
        proj_commits = os.listdir(before_src)
        for proj_commit in proj_commits:
            before_files = os.listdir(before_src + "\\" + proj_commit)
            for before_file in before_files:
                print(proj, proj_commit, before_file)
                src_path = before_src + "\\" + proj_commit + "\\" + before_file
                dest_path = projs_path + "\\after\\" + proj[1] + "\\" + proj_commit + "\\" + before_file
                try:
                    src_infile = open(src_path, 'r', encoding='utf-8')
                    dest_infile = open(dest_path, 'r', encoding='utf-8')
                except:
                    print("File read error")
                    continue
                src_lines_list = src_infile.readlines()
                dest_lines_list = dest_infile.readlines()
                start_time = time.perf_counter()
                result = subprocess.run(
                    "git diff --no-index --diff-algorithm=myers --unified=0 --numstat %s %s" % (
                        src_path, dest_path), text=True, stdout=subprocess.PIPE, encoding='utf-8')
                end_time = time.perf_counter()
                result_list = str(result.stdout).splitlines()
                if result_list:
                    ins_num, del_num = result_list[0].split()[:2]
                    edit_scripts = []
                    for result_line in result_list:
                        if result_line.startswith("@@"):
                            edit_scripts.append(re.match(r'@@.*@@', result_line).group())
                    run_time = end_time - start_time
                    sql = '''Insert into MyersEditScripts (ProjId, ProjName, CommitSha, FileName, FileLengthBefore, FileLengthAfter, nInserts, nDeletes, nEdits, EditScript, RunTime) \
                                        values ('%s', '%s', '%s', '%s', %d, %d, %d, %d, %d, "%s", %s)''' \
                          % (proj[0], proj[1], proj_commit, before_file, len(src_lines_list), len(dest_lines_list),
                             int(ins_num), int(del_num),
                             int(ins_num) + int(del_num), str(edit_scripts), str(run_time))
                    print(sql)
                    cursor.execute(sql)
            conn.commit()


def run_git_Myers_runtime(projs_path, lang, i):
    cursor.execute("select Id, Name from Projects where Language = '%s'" % (lang))
    projs = cursor.fetchall()
    for proj in projs:
        before_src = projs_path + "\\before\\" + proj[1]
        proj_commits = os.listdir(before_src)
        for proj_commit in proj_commits:
            before_files = os.listdir(before_src + "\\" + proj_commit)
            for before_file in before_files:
                cursor.execute("select Id from myerseditscripts where ProjId = '%s' and ProjName = '%s' and CommitSha = '%s' and FileName = '%s' and Language = '%s' and DiffId is not NULL;" % (proj[0], proj[1], proj_commit, before_file, lang))
                analysis_record = cursor.fetchone()
                if not analysis_record:
                    continue
                print(proj, proj_commit, before_file)
                src_path = before_src + "\\" + proj_commit + "\\" + before_file
                dest_path = projs_path + "\\after\\" + proj[1] + "\\" + proj_commit + "\\" + before_file
                try:
                    src_infile = open(src_path, 'r', encoding='utf-8')
                    dest_infile = open(dest_path, 'r', encoding='utf-8')
                except:
                    print("File read error")
                    continue
                start_time = time.perf_counter()
                result = subprocess.run(
                    "git diff --no-index --diff-algorithm=myers --unified=0 --numstat %s %s" % (
                        src_path, dest_path), text=True, stdout=subprocess.PIPE, encoding='utf-8')
                end_time = time.perf_counter()
                result_list = str(result.stdout).splitlines()
                if result_list:
                    run_time = end_time - start_time
                    sql = "update myerseditscripts set RunTime%s = '%s' where ProjId = '%s' and ProjName = '%s' and CommitSha = '%s' and FileName = '%s' and Language = '%s';" % (
                        str(i), run_time, proj[0], proj[1], proj_commit, before_file, lang)
                    print(sql)
                    cursor.execute(sql)
            conn.commit()


def run_BDiff_runtime(projs_path, lang, i):
    cursor.execute("select Id, Name from Projects where Language = '%s'" % (lang))
    projs = cursor.fetchall()
    for proj in projs:
        before_src = projs_path + "\\before\\" + proj[1]
        proj_commits = os.listdir(before_src)
        for proj_commit in proj_commits:
            before_files = os.listdir(before_src + "\\" + proj_commit)
            for before_file in before_files:
                cursor.execute("select Id from BDiffEditScripts_copy2 where ProjId = '%s' and ProjName = '%s' and CommitSha = '%s' and FileName = '%s' and Language = '%s' and DiffId is not NULL;" % (proj[0], proj[1], proj_commit, before_file, lang))
                analysis_record = cursor.fetchone()
                if not analysis_record:
                    continue
                print(proj, proj_commit, before_file)

                try:
                    src_infile = open(before_src + "\\" + proj_commit + "\\" + before_file, 'r', encoding='utf-8')
                    dest_infile = open(projs_path + "\\after\\" + proj[1] + "\\" + proj_commit + "\\" + before_file,
                                       'r', encoding='utf-8')
                except:
                    continue
                src_lines_list = src_infile.read().splitlines()
                dest_lines_list = dest_infile.read().splitlines()
                src_infile.close()
                dest_infile.close()
                start_time = time.perf_counter()
                edit_scripts = BDiff.BDiff(before_src + "\\" + proj_commit + "\\" + before_file,
                                              projs_path + "\\after\\" + proj[
                                                  1] + "\\" + proj_commit + "\\" + before_file, src_lines_list,
                                              dest_lines_list, pure_cp_block_contain_punc=False,
                                              pure_mv_block_contain_punc=False)
                end_time = time.perf_counter()
                run_time = end_time - start_time
                sql = "update BDiffEditScripts_copy2 set RunTime%s = '%s' where ProjId = '%s' and ProjName = '%s' and CommitSha = '%s' and FileName = '%s' and Language = '%s';" % (
                    str(i), run_time, proj[0], proj[1], proj_commit, before_file, lang)
                cursor.execute(sql)
            conn.commit()


def run_BDiff(projs_path, lang):
    cursor.execute("select Id, Name from Projects where Language = '%s'" % (lang))
    projs = cursor.fetchall()
    for proj in projs:
        before_src = projs_path + "\\before\\" + proj[1]
        proj_commits = os.listdir(before_src)
        for proj_commit in proj_commits:
            before_files = os.listdir(before_src + "\\" + proj_commit)
            for before_file in before_files:
                print(proj, proj_commit, before_file)

                try:
                    src_infile = open(before_src + "\\" + proj_commit + "\\" + before_file, 'r', encoding='utf-8')
                    dest_infile = open(projs_path + "\\after\\" + proj[1] + "\\" + proj_commit + "\\" + before_file,
                                       'r',
                                       encoding='utf-8')
                except:
                    continue
                src_lines_list = src_infile.read().splitlines()
                dest_lines_list = dest_infile.read().splitlines()
                src_infile.close()
                dest_infile.close()
                start_time = time.perf_counter()
                edit_scripts = BDiffgit.BDiff(before_src + "\\" + proj_commit + "\\" + before_file,
                                              projs_path + "\\after\\" + proj[
                                                  1] + "\\" + proj_commit + "\\" + before_file, src_lines_list,
                                              dest_lines_list, pure_cp_block_contain_punc=False,
                                              pure_mv_block_contain_punc=False)
                end_time = time.perf_counter()
                run_time = end_time - start_time
                nDeletes, nInserts, nCopies, nMoves, nUpdates, nCUpdates, nMUpdates, nSplits, nMerges = 0, 0, 0, 0, 0, 0, 0, 0, 0
                for edit_script in edit_scripts:
                    if edit_script['mode'] == 'delete':
                        nDeletes += 1
                    elif edit_script['mode'] == 'insert':
                        nInserts += 1
                    elif edit_script['mode'] == 'copy':
                        cpy_sql = '''Insert into copyactions (ProjId, ProjName, CommitSha, FileName, ActionScript, SrcLine, DestLine, BlockLength, nUpdates) values ('%s', '%s', '%s', '%s', "%s", %d, %d, %d, %d)''' \
                                  % (
                                  proj[0], proj[1], proj_commit, before_file, str(edit_script), edit_script['src_line'],
                                  edit_script['dest_line'], edit_script['block_length'], 0)
                        cursor.execute(cpy_sql)
                        nCopies += 1
                    elif edit_script['mode'] == 'move':
                        move_sql = '''Insert into moveactions (ProjId, ProjName, CommitSha, FileName, ActionScript, SrcLine, DestLine, BlockLength, nUpdates) values ('%s', '%s', '%s', '%s', "%s", %d, %d, %d, %d)''' \
                                   % (proj[0], proj[1], proj_commit, before_file, str(edit_script),
                                      edit_script['src_line'], edit_script['dest_line'], edit_script['block_length'], 0)
                        cursor.execute(move_sql)
                        nMoves += 1
                    elif edit_script['mode'] == 'update':
                        update_sql = '''Insert into updateactions (ProjId, ProjName, CommitSha, FileName, Type, ActionScript, SrcLine, DestLine) values ('%s', '%s', '%s', '%s', '%s', "%s", %d, %d)''' \
                                     % (proj[0], proj[1], proj_commit, before_file, 'update', str(edit_script),
                                        edit_script['src_line'], edit_script['dest_line'])
                        cursor.execute(update_sql)
                        nUpdates += 1
                    elif edit_script['mode'] == 'c_update':
                        c_update_sql = '''Insert into updateactions (ProjId, ProjName, CommitSha, FileName, Type, ActionScript, SrcLine, DestLine) values ('%s', '%s', '%s', '%s', '%s', "%s", %d, %d)''' \
                                       % (proj[0], proj[1], proj_commit, before_file, 'c_update', str(edit_script),
                                          edit_script['src_line'], edit_script['dest_line'])
                        cursor.execute(c_update_sql)
                        nCUpdates += 1
                    elif edit_script['mode'] == 'm_update':
                        m_update_sql = '''Insert into updateactions (ProjId, ProjName, CommitSha, FileName, Type, ActionScript, SrcLine, DestLine) values ('%s', '%s', '%s', '%s', '%s', "%s", %d, %d)''' \
                                       % (proj[0], proj[1], proj_commit, before_file, 'm_update', str(edit_script),
                                          edit_script['src_line'], edit_script['dest_line'])
                        cursor.execute(m_update_sql)
                        nMUpdates += 1
                    elif edit_script['mode'] == 'split':
                        nSplits += 1
                    elif edit_script['mode'] == 'merge':
                        nMerges += 1
                sql = '''Insert into BDiffEditScripts (ProjId, ProjName, CommitSha, FileName, FileLengthBefore, FileLengthAfter, nEdits, nDeletes, nInserts, nCopies, nMoves, nUpdates, nCUpdates, nMUpdates, nSplits, nMerges, EditScript, RunTime) \
                                    values ('%s', '%s', '%s', '%s', %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, "%s", '%s')''' \
                      % (proj[0], proj[1], proj_commit, before_file, len(src_lines_list), len(dest_lines_list),
                         len(edit_scripts), nDeletes, nInserts, nCopies, nMoves, nUpdates, nCUpdates, nMUpdates,
                         nSplits, nMerges, str(edit_scripts), str(run_time))
                cursor.execute(sql)
            conn.commit()


def left_spaces(str, is_horizontal):
    if str == "\n":
        return 0
    elif str.strip() == "" and len(str) > 0:
        return len(str) - 1
    first_char = str.lstrip()[0]
    first_char_index = str.index(first_char)
    return first_char_index


def min_leftspaces(start_line, block_length, dest_lines_list, is_horizontal = False):
    leftspaces = [left_spaces(dest_line["dest_line"], is_horizontal) for dest_line in
                  dest_lines_list[start_line:start_line + block_length]]
    return min(leftspaces)


def has_conflict(dest_start, dest_lines_list):
    if dest_lines_list[dest_start - 1]["ea_id"] != None and dest_lines_list[dest_start - 1]["ea_id"] == \
            dest_lines_list[dest_start]["ea_id"]:
        return True
    else:
        return False


def move_over_spaces(dest_index, dest_line_no, dest_lines_list):
    if dest_line_no < dest_index:
        for dest_line in dest_lines_list[dest_index - 1: dest_line_no: -1]:
            if dest_line["dest_line"].strip() != "":
                return False
    else:
        for dest_line in dest_lines_list[dest_index + 1: dest_line_no]:
            if dest_line["dest_line"].strip() != "":
                return False
    return True

def can_be_moved(dest_start, block_length, dest_lines_list):
    for d_l in dest_lines_list[dest_start: dest_start + block_length]:
        if d_l["ea_id"]:
            return False
    return True

def generate_random_string(length: int) -> str:
    if not isinstance(length, int):
        raise ValueError("Length must be integer!")
    elif length <= 0:
        return ""
    characters = string.ascii_letters + string.digits + ' !@#$%^&*()_+-=[]{}|;:,.<>?`~'
    random_string = ''.join(random.choice(characters) for _ in range(length))
    return random_string

# edits: ["delete", "insert", "update", "split", "move", "copy"]
def generate_edit_script(file, nedits, src_output, dest_output, max_split_line_num=8,
                         max_merge_line_num=8):
    infile_lines = open(file, 'r', encoding='utf-8').readlines()
    file_length = len(infile_lines)
    src_lines_dict = OrderedDict()
    dest_lines_list = []
    edit_script = []
    for line_no, line in enumerate(infile_lines):
        src_lines_dict[line_no + 1] = {"src_line": line, "dest_line": line, "dest_line_no": line_no + 1, "mode": None,
                                       "ea_id": None}
        dest_lines_list.append(
            {"src_line": line, "dest_line": line, "src_line_no": line_no + 1, "mode": None, "ea_id": None})
    if len(infile_lines) < nedits:
        return None
    # 生成src的编辑
    edits = ["delete", "insert", "update", "split", "merge", "move", "copy"]
    src_start = 1
    i = 1
    i_mupdate = 1
    i_cupdate = 1
    while src_start <= file_length and i <= nedits:
        src_edit = random.choice(edits)
        line_no = random.randint(src_start, file_length)
        if src_edit == "delete":
            for dest_index, dest_line in enumerate(dest_lines_list):
                if dest_line["src_line_no"] == line_no:
                    if dest_line["mode"]:
                        break
                    edit_script.append({"src_line": line_no, "mode": "delete", "ea_id": "s" + str(i)})
                    src_lines_dict[line_no]["mode"] = "delete"
                    src_lines_dict[line_no]["ea_id"] = "s" + str(i)
                    del dest_lines_list[dest_index]
                    break
            src_start = line_no + 1
        elif src_edit == "insert":
            line_len = random.randint(0,30)
            for dest_index, dest_line in enumerate(dest_lines_list):
                if dest_line["src_line_no"] == line_no:
                    if dest_line["mode"]:
                        break
                    edit_script.append({"dest_line": dest_index + 1, "mode": "insert", "ea_id": "s" + str(i)})
                    dest_lines_list.insert(dest_index, {"dest_line":generate_random_string(line_len) + "\n", "mode":"insert", "ea_id":"s" + str(i), "src_line": src_lines_dict[line_no]["src_line"], "src_line_no":line_no})
                    break
            src_start = line_no + 1
        elif src_edit == "update":
            for dest_index, dest_line in enumerate(dest_lines_list):
                if dest_line["mode"] != None or dest_line["dest_line"].strip() == "":
                    continue
                elif dest_line["src_line_no"] == line_no:
                    dest_line["mode"] = "update"
                    str_diff, updated_str = genertate_updated_str(dest_line["dest_line"].rstrip())
                    dest_line["dest_line"] = updated_str + "\n"
                    dest_line["ea_id"] = "s" + str(i)
                    edit_script.append(
                        {"src_line": line_no, "dest_line": dest_index + 1, "mode": "update", "ea_id": "s" + str(i), "str_diff":str_diff})
                    src_lines_dict[line_no]["mode"] = "update"
                    src_lines_dict[line_no]["ea_id"] = "s" + str(i)
                    break
            src_start = line_no + 1
        elif src_edit == "split":
            src_line_strip = src_lines_dict[line_no]['src_line'].strip()
            if len(src_line_strip) <= 1:
                continue
            split_times = random.randint(1, min(len(src_line_strip) - 1, max_split_line_num))
            indexes = sorted(np.random.choice(np.arange(1, len(src_line_strip)), split_times, replace=False))
            start_index = 0
            dest_line_index, dest_line_str = None, None
            found_dest = False
            for i2, index in enumerate(indexes):
                if not found_dest:
                    for dest_index, dest_line in enumerate(dest_lines_list):
                        if dest_line["src_line_no"] == line_no:
                            dest_line_index = dest_index
                            dest_line_str = dest_line["dest_line"].strip()
                            src_lines_dict[line_no]["mode"] = "split"
                            src_lines_dict[line_no]["ea_id"] = "s" + str(i)
                            src_lines_dict[line_no]["block_length"] = split_times + 1
                            del dest_lines_list[dest_line_index]
                            found_dest = True
                            break
                dest_lines_list.insert(dest_line_index + i2, {"dest_line": dest_line_str[start_index: index] + "\n",
                                                              "src_line": src_lines_dict[line_no]["src_line"],
                                                              "src_line_no": line_no, "mode": "split",
                                                              "block_length": split_times + 1,
                                                              "ea_id": "s" + str(i)})
                start_index = index
            dest_lines_list.insert(dest_line_index + i2 + 1, {
                "dest_line": dest_line_str[start_index: len(src_lines_dict[line_no]['src_line'])] + "\n",
                "src_line": src_lines_dict[line_no]["src_line"], "src_line_no": line_no, "mode": "split",
                "ea_id": "s" + str(i), "block_length": split_times + 1})
            edit_script.append({"src_line": line_no, "dest_line": dest_line_index + 1, "block_length": split_times + 1,
                                "mode": "split", "ea_id": "s" + str(i)})
            src_start = line_no + 1 + split_times
        elif file_length - line_no < 2:
            break
        elif src_edit == "merge":
            block_length = random.randint(2, min(file_length - line_no, max_merge_line_num))
            pure_block_length = block_length
            is_last_space = True
            for line_m in range(line_no + block_length - 1, line_no - 1, -1):
                if src_lines_dict[line_m]["src_line"].strip() == "":
                    pure_block_length -= 1
                    if is_last_space:
                        block_length -= 1
                else:
                    is_last_space = False
            if pure_block_length < 2:
                continue
            merged_line = ""
            for merge_i in range(line_no, line_no + block_length):
                merged_line = merged_line + src_lines_dict[merge_i]["src_line"].strip()
                src_lines_dict[merge_i]["mode"] = "merge"
                src_lines_dict[merge_i]["ea_id"] = "s" + str(i)
                src_lines_dict[merge_i]["block_length"] = block_length
            merged_line = merged_line + "\n"
            for dest_index, dest_line in enumerate(dest_lines_list):
                if dest_line["src_line_no"] == line_no:
                    src_lines_dict[line_no]["dest_line_no"] = dest_index + 1
                    dest_lines_list[dest_index]["dest_line"] = merged_line
                    dest_lines_list[dest_index]["ea_id"] = "s" + str(i)
                    dest_lines_list[dest_index]["mode"] = "merge"
                    dest_lines_list[dest_index]["block_length"] = block_length
                    break
            # 删除多余的，从后往前删
            for del_i in range(dest_index + block_length - 1, dest_index, -1):
                del dest_lines_list[del_i]
            edit_script.append(
                {"mode": "merge", "src_line": line_no, "block_length": block_length, "ea_id": "s" + str(i),
                 "dest_line": dest_index + 1})
            src_start = line_no + block_length
        elif src_edit == "move":
            directions = ["h", "u", "d"]
            direction = random.choice(directions)
            block_length = random.randint(2, file_length - line_no)
            if cal_pure_block_length(src_lines_dict, line_no, block_length) < 2:
                continue
            tab_offset = None
            dest_start = None
            has_update = random.choice([True, False])
            if direction == "h":
                moved = True
                for dest_index, dest_line in enumerate(dest_lines_list):
                    if dest_line["src_line_no"] == line_no:
                        dest_line_index = dest_index
                        dest_start = dest_index
                        if not can_be_moved(dest_start, block_length, dest_lines_list):
                            moved = False
                            break
                        tab_offs = list(range(-min_leftspaces(dest_start, block_length, dest_lines_list, True), 11))
                        tab_offs.remove(0)
                        tab_offset = random.choice(tab_offs)
                        for i_offset in range(block_length):
                            dest_i = dest_index + i_offset
                            src_i = line_no + i_offset
                            if has_update and random.choice([True, False]) and len(dest_lines_list[dest_i]["dest_line"].strip()) >= 8:
                                str_diff, updated_str = genertate_block_updated_str(dest_lines_list[dest_i]["dest_line"].rstrip())
                                dest_lines_list[dest_i]["dest_line"] = updated_str + "\n"
                                edit_script.append({"mode": "m_update", "src_line": src_i, "ea_id": "s" + str(i),
                                                    "mu_id": "mu_" + str(i_mupdate), "str_diff":str_diff,
                                                    "dest_line": dest_i + 1})
                                dest_lines_list[dest_i]["mu_id"] = "mu_" + str(i_mupdate)
                                dest_lines_list[dest_i]["mode"] = "m_update"
                                src_lines_dict[src_i]["mu_id"] = "mu_" + str(i_mupdate)
                                i_mupdate += 1
                            if tab_offset < 0:
                                dest_lines_list[dest_i]["dest_line"] = dest_lines_list[dest_i]["dest_line"][abs(tab_offset):]
                            else:
                                dest_lines_list[dest_i]["dest_line"] = " " * tab_offset + dest_lines_list[dest_i][
                                    "dest_line"]
                            dest_lines_list[dest_i]["mode"] = "move" if dest_lines_list[dest_i]["mode"] == None else \
                            dest_lines_list[dest_i]["mode"]
                            dest_lines_list[dest_i]["ea_id"] = "s" + str(i)
                            dest_lines_list[dest_i]["block_length"] = block_length
                            dest_lines_list[dest_i]["tab_offset"] = tab_offset
                            src_lines_dict[src_i]["mode"] = dest_lines_list[dest_i]["mode"]
                            src_lines_dict[src_i]["ea_id"] = "s" + str(i)
                            src_lines_dict[src_i]["block_length"] = block_length
                            src_lines_dict[src_i]["dest_line_no"] = dest_i + 1
                            src_lines_dict[src_i]["tab_offset"] = tab_offset
                        break
                if not moved:
                    continue
            if direction == "u":
                moved = True
                for dest_index, dest_line in enumerate(dest_lines_list):
                    if dest_line["src_line_no"] == line_no:
                        dest_line_index = dest_index
                        if not can_be_moved(dest_line_index, block_length, dest_lines_list):
                            moved = False
                            break
                        tab_offset = random.randrange(-min_leftspaces(dest_line_index, block_length, dest_lines_list),
                                                      11)
                        break
                if not moved or dest_line_index == 0:
                    continue
                dest_start = random.randint(0, dest_line_index - 1)

                if has_conflict(dest_start, dest_lines_list) or move_over_spaces(dest_line_index, dest_start, dest_lines_list):
                    continue
                else:
                    # extract move_target
                    move_target_block = dest_lines_list[dest_line_index: dest_line_index + block_length]
                    del dest_lines_list[dest_line_index: dest_line_index + block_length]
                    for index_offset, move_target in enumerate(move_target_block):
                        if has_update and random.choice([True, False]) and len(move_target["dest_line"].strip()) >= 8:
                            str_diff, updated_str = genertate_block_updated_str(move_target["dest_line"].rstrip())
                            move_target["dest_line"] = updated_str + "\n"
                            edit_script.append(
                                {"mode": "m_update", "src_line": line_no + index_offset, "ea_id": "s" + str(i),
                                 "mu_id": "mu_" + str(i_mupdate), "str_diff":str_diff,
                                 "dest_line": dest_start + index_offset + 1})
                            move_target["mu_id"] = "mu_" + str(i_mupdate)
                            move_target["mode"] = "m_update"
                            src_lines_dict[line_no + index_offset]["mu_id"] = "mu_" + str(i_mupdate)
                            i_mupdate += 1
                        if tab_offset < 0:
                            if move_target["dest_line"].strip() != "":
                                move_target["dest_line"] = move_target["dest_line"][abs(tab_offset):]
                        else:
                            move_target["dest_line"] = " " * tab_offset + move_target["dest_line"]
                        if not move_target["dest_line"].endswith("\n"):
                            move_target["dest_line"] = move_target["dest_line"] + "\n"
                        move_target["mode"] = "move" if move_target["mode"] == None else move_target["mode"]
                        move_target["ea_id"] = "s" + str(i)
                        move_target["block_length"] = block_length
                        move_target["tab_offset"] = tab_offset
                        src_lines_dict[line_no + index_offset]["mode"] = move_target["mode"]
                        src_lines_dict[line_no + index_offset]["ea_id"] = "s" + str(i)
                        src_lines_dict[line_no + index_offset]["block_length"] = block_length
                        src_lines_dict[line_no + index_offset]["dest_line_no"] = dest_start + index_offset + 1
                        src_lines_dict[line_no + index_offset]["tab_offset"] = tab_offset
                    # 列表拼接
                    if not move_target_block[-1]["dest_line"].endswith("\n"):
                        move_target_block[-1]["dest_line"] = move_target_block[-1]["dest_line"] + "\n"
                    dest_lines_list = dest_lines_list[:dest_start] + move_target_block + dest_lines_list[dest_start:]
            if direction == "d":
                moved = True
                for dest_index, dest_line in enumerate(dest_lines_list):
                    if dest_line["src_line_no"] == line_no:
                        dest_line_index = dest_index
                        if not can_be_moved(dest_line_index, block_length, dest_lines_list):
                            moved = False
                            break
                        tab_offset = random.randrange(-min_leftspaces(dest_line_index, block_length, dest_lines_list),
                                                      11)
                        break
                if not moved or dest_line_index + block_length + 1 >= len(dest_lines_list):
                    continue
                dest_start = random.randint(dest_line_index + block_length + 1, len(dest_lines_list) - 1)
                if has_conflict(dest_start, dest_lines_list) or move_over_spaces(dest_line_index + block_length, dest_start, dest_lines_list):
                    continue
                else:
                    # extract move_target
                    move_target_block = copy.deepcopy(dest_lines_list[dest_line_index: dest_line_index + block_length])
                    for index_offset, move_target in enumerate(move_target_block):
                        if has_update and random.choice([True, False]) and len(move_target["dest_line"].strip()) >= 8:
                            str_diff, updated_str = genertate_block_updated_str(move_target["dest_line"].rstrip())
                            move_target["dest_line"] = updated_str + "\n"
                            edit_script.append(
                                {"mode": "m_update", "src_line": line_no + index_offset, "ea_id": "s" + str(i),
                                 "mu_id": "mu_" + str(i_mupdate), "str_diff":str_diff,
                                 "dest_line": dest_start + index_offset + 1})
                            move_target["mu_id"] = "mu_" + str(i_mupdate)
                            move_target["mode"] = "m_update"
                            src_lines_dict[line_no + index_offset]["mu_id"] = "mu_" + str(i_mupdate)
                            i_mupdate += 1
                        if tab_offset < 0:
                            if move_target["dest_line"].strip() != "":
                                move_target["dest_line"] = move_target["dest_line"][abs(tab_offset):]
                        else:
                            move_target["dest_line"] = " " * tab_offset + move_target["dest_line"]
                        move_target["mode"] = "move" if move_target["mode"] in ("copy", None) else move_target["mode"]
                        move_target["ea_id"] = "s" + str(i)
                        move_target["block_length"] = block_length
                        move_target["tab_offset"] = tab_offset
                        src_lines_dict[line_no + index_offset]["mode"] = move_target["mode"]
                        src_lines_dict[line_no + index_offset]["ea_id"] = "s" + str(i)
                        src_lines_dict[line_no + index_offset]["block_length"] = block_length
                        src_lines_dict[line_no + index_offset]["dest_line_no"] = dest_start + index_offset + 1
                        src_lines_dict[line_no + index_offset]["tab_offset"] = tab_offset
                    # 列表拼接
                    if not dest_lines_list[dest_start - 1]["dest_line"].endswith("\n"):
                        dest_lines_list[dest_start - 1]["dest_line"] = dest_lines_list[dest_start - 1][
                                                                           "dest_line"] + "\n"
                    dest_lines_list = dest_lines_list[:dest_start] + move_target_block + dest_lines_list[dest_start:]
                    del dest_lines_list[dest_line_index: dest_line_index + block_length]
            edit_script.append(
                {"mode": "move", "src_line": line_no, "block_length": block_length, "ea_id": "s" + str(i),
                 "dest_line": dest_start + 1, "indent_offset": tab_offset})
            src_start = line_no + block_length
        elif src_edit == "copy":
            copy_src_line_no = random.randint(1, len(src_lines_dict))
            if file_length - copy_src_line_no + 1 < 2:
                continue
            block_length = random.randint(2, file_length - copy_src_line_no + 1)
            if cal_pure_block_length(src_lines_dict, copy_src_line_no, block_length) < 2:
                continue
            tab_offset = None
            dest_start = random.randint(0, len(dest_lines_list)-1)
            if has_conflict(dest_start, dest_lines_list):
                continue
            copy_target_block = [
                {"dest_line": src_lines_dict[s_line_no]["src_line"], "src_line_no": str(s_line_no) + "_copy"} for
                s_line_no in range(copy_src_line_no, copy_src_line_no + block_length)]
            tab_offset = random.randrange(-min_leftspaces(0, block_length, copy_target_block), 11)
            has_update = random.choice([True, False])
            for index_offset, copy_target in enumerate(copy_target_block):
                if has_update and random.choice([True, False]) and len(copy_target["dest_line"].strip()) >= 8:
                    str_diff, updated_str = genertate_block_updated_str(copy_target["dest_line"].rstrip())
                    copy_target["dest_line"] = updated_str + "\n"
                    edit_script.append(
                        {"mode": "c_update", "src_line": copy_src_line_no + index_offset, "ea_id": "s" + str(i),
                         "cu_id": "cu_" + str(i_cupdate), "str_diff":str_diff,
                         "dest_line": dest_start + index_offset + 1})
                    copy_target["cu_id"] = "cu_" + str(i_cupdate)
                    copy_target["mode"] = "c_update"
                    src_lines_dict[copy_src_line_no + index_offset]["cu_id"] = copy_target["cu_id"]
                    i_cupdate += 1
                if tab_offset < 0:
                    if copy_target["dest_line"].strip() != "":
                        copy_target["dest_line"] = copy_target["dest_line"][abs(tab_offset):]
                else:
                    copy_target["dest_line"] = " " * tab_offset + copy_target["dest_line"]
                if not copy_target["dest_line"].endswith("\n"):
                    copy_target["dest_line"] = copy_target["dest_line"] + "\n"
                copy_target["mode"] = "copy" if "mode" not in copy_target else copy_target["mode"]
                copy_target["ea_id"] = "s" + str(i)
                copy_target["block_length"] = block_length
                copy_target["tab_offset"] = tab_offset
                src_lines_dict[copy_src_line_no + index_offset]["mode"] = copy_target["mode"]
                src_lines_dict[copy_src_line_no + index_offset]["ea_id"] = "s" + str(i)
                src_lines_dict[copy_src_line_no + index_offset]["block_length"] = block_length
                src_lines_dict[copy_src_line_no + index_offset]["dest_line_no"] = dest_start + index_offset + 1
                src_lines_dict[copy_src_line_no + index_offset]["tab_offset"] = tab_offset
            dest_lines_list = dest_lines_list[:dest_start] + copy_target_block + dest_lines_list[dest_start:]
            edit_script.append(
                {"mode": "copy", "src_line": copy_src_line_no, "block_length": block_length, "ea_id": "s" + str(i),
                 "dest_line": dest_start + 1, "indent_offset": tab_offset})
        i += 1
    src_outfile = open(src_output, "w", encoding='utf-8')
    for src_line_key in src_lines_dict:
        src_outfile.write(src_lines_dict[src_line_key]["src_line"])
    src_outfile.close()
    dest_outfile = open(dest_output, "w", encoding='utf-8')
    for dest_line in dest_lines_list:
        dest_outfile.write(dest_line["dest_line"])
    dest_outfile.close()
    # 更新编辑脚本
    for es in edit_script:
        for di, dl in enumerate(dest_lines_list):
            if es["mode"] == "m_update" and "mu_id" in dl and es["mu_id"] == dl["mu_id"]:
                es["dest_line"] = di + 1
                break
            elif es["mode"] == "c_update" and "cu_id" in dl and es["cu_id"] == dl["cu_id"]:
                es["dest_line"] = di + 1
                break
            elif es["mode"] not in ["m_update", "c_update"] and es["ea_id"] == dl["ea_id"]:
                es["dest_line"] = di + 1
                break
    return edit_script

def is_pure_punctuation(s):
    if not s:
        return True
    pattern = r'^[~`!@#$%^&*()-_+={}\[\]|\\:;"\'<,>.?/\n\s]+$'
    return bool(re.match(pattern, s))

def cal_pure_block_length(src_lines_dict, line_no, block_length):
    for src_line in range(line_no, line_no + block_length):
        if src_line in src_lines_dict and (src_lines_dict[src_line]["src_line"].strip() == "" or is_pure_punctuation(src_lines_dict[src_line]["src_line"].strip())):
            block_length -= 1
    return block_length


def unified_EA(ES):
    unified_ES = []
    for EA in ES:
        if EA["mode"] == "insert":
            unified_ES.append({"mode": "insert", "dest_line": EA["dest_line"]})
        elif EA["mode"] == "delete":
            unified_ES.append({"mode": "delete", "src_line": EA["src_line"]})
        elif EA["mode"] == "update":
            unified_ES.append({"mode": "update", "src_line": EA["src_line"], "dest_line": EA["dest_line"]})
        elif EA["mode"] == "split":
            unified_ES.append({"mode": "split", "src_line": EA["src_line"], "dest_line": EA["dest_line"],
                               "block_length": EA["block_length"]})
        elif EA["mode"] == "merge":
            unified_ES.append({"mode": "merge", "src_line": EA["src_line"], "dest_line": EA["dest_line"],
                               "block_length": EA["block_length"]})
        elif EA["mode"] == "move":
            unified_ES.append({"mode": "move", "src_line": EA["src_line"], "dest_line": EA["dest_line"],
                               "block_length": EA["block_length"]})
        elif EA["mode"] == "copy":
            unified_ES.append({"mode": "copy", "src_line": EA["src_line"], "dest_line": EA["dest_line"],
                               "block_length": EA["block_length"]})
        elif EA["mode"] == "m_update":
            unified_ES.append({"mode": "m_update", "src_line": EA["src_line"], "dest_line": EA["dest_line"]})
        elif EA["mode"] == "c_update":
            unified_ES.append({"mode": "c_update", "src_line": EA["src_line"], "dest_line": EA["dest_line"]})
    return unified_ES

def genertate_updated_str(str):
    characters = string.digits + string.ascii_letters + "+-*/<>_!@#$%^&()=[]| "
    update_action = random.choice(["delete", "insert", "update"])
    if update_action == "delete":
        if len(str.strip()) < 3:
            update_action = "insert"
        else:
            deleted_len = random.randint(1, len(str) // 3)
            deleted_start = random.randint(0, len(str) - deleted_len)
            return [[[deleted_start, deleted_start + deleted_len - 1]],[[]]], str[:deleted_start] + str[deleted_start+deleted_len:]
    if update_action == "update":
        if len(str.strip()) < 3:
            update_action = "insert"
        else:
            deleted_len = random.randint(1, len(str) // 3)
            updated_start = random.randint(0, len(str) - deleted_len)
            updated_len = random.randint(1, len(str) // 3)
            updated_str = ''.join(random.choices(characters, k=updated_len))
            return [[[updated_start, updated_start + deleted_len - 1]], [[updated_start, updated_start + updated_len - 1]]], str[:updated_start] + updated_str + str[updated_start + deleted_len + 1:]
    if update_action == "insert":
        inserted_len = 1 if len(str) < 3 else random.randint(1, len(str) // 3)
        inserted_start = random.randint(0, len(str))
        inserted_str = ''.join(random.choices(characters, k=inserted_len))
        return [[[]],[[inserted_start, inserted_start + inserted_len - 1]]], str[:inserted_start] + inserted_str + str[inserted_start:]

def genertate_block_updated_str(str):
    characters = string.digits + string.ascii_letters + "+-*/<>_!@#$%^&()=[]|"
    update_action = random.choice(["delete", "insert", "update"])
    if update_action == "delete":
        if len(str.strip()) == 1:
            update_action = "insert"
        else:
            start_index = 0
            if str.startswith(" ") or str.startswith("\t"):
                first_chara_index = str.find(str.lstrip()[0])
                n_spaces = str[:first_chara_index].count(" ")
                n_tabs = str[:first_chara_index].count("\t")
                start_index = n_spaces + n_tabs + 1
            deleted_len = random.randint(1, len(str[start_index:]) // 3)
            deleted_start = random.randint(start_index, len(str) - deleted_len)
            return [[[deleted_start, deleted_start + deleted_len - 1]],[[]]], str[:deleted_start] + str[deleted_start+deleted_len:]
    if update_action == "update":
        if len(str.strip()) == 1:
            update_action = "insert"
        else:
            start_index = 1
            if str.startswith(" ") or str.startswith("\t"):
                first_chara_index = str.find(str.lstrip()[0])
                n_spaces = str[:first_chara_index].count(" ")
                n_tabs = str[:first_chara_index].count("\t")
                start_index = n_spaces + n_tabs
            deleted_len = random.randint(1, len(str[start_index:]) // 3)
            updated_start = random.randint(start_index, len(str) - deleted_len)
            updated_len = random.randint(1, len(str[start_index:]) // 3)
            updated_str = ''.join(random.choices(characters, k=updated_len))
            return [[[updated_start, updated_start + deleted_len - 1]], [[updated_start, updated_start + updated_len - 1]]], str[:updated_start] + updated_str + str[updated_start + deleted_len + 1:]
    if update_action == "insert":
        start_index = 0
        if str.startswith(" ") or str.startswith("\t"):
            first_chara_index = str.find(str.lstrip()[0])
            n_spaces = str[:first_chara_index].count(" ")
            n_tabs = str[:first_chara_index].count("\t")
            start_index = n_spaces + n_tabs
        inserted_len = random.randint(1, len(str[start_index:]) // 3)
        inserted_start = random.randint(start_index, len(str))
        inserted_str = ''.join(random.choices(characters, k=inserted_len))
        return [[[]],[[inserted_start, inserted_start + inserted_len - 1]]], str[:inserted_start] + inserted_str + str[inserted_start:]


def cal_identical_percentage(generated_ES, BDiff_ES):
    if not generated_ES:
        return -1
    identified_number = 0
    for gen_ea in generated_ES:
        if gen_ea in BDiff_ES:
            identified_number += 1
        # else:
        #     print("mismatched:", bdiff_ea)
    return identified_number/len(generated_ES)

def cal_indicators(edit_script):
    nDeletes, nInserts, nCopies, nMoves, nUpdates, nCUpdates, nMUpdates, nSplits, nMerges = 0, 0, 0, 0, 0, 0, 0, 0, 0
    for es in edit_script:
        if es["mode"] == "delete":
            nDeletes += 1
        elif es["mode"] == "insert":
            nInserts += 1
        elif es["mode"] == "copy":
            nCopies += 1
        elif es["mode"] == "move":
            nMoves += 1
        elif es["mode"] == "update":
            nUpdates += 1
        elif es["mode"] == "c_update":
            nCUpdates += 1
        elif es["mode"] == "m_update":
            nMUpdates += 1
        elif es["mode"] == "split":
            nSplits += 1
        elif es["mode"] == "merge":
            nMerges += 1
    return len(edit_script), nDeletes, nInserts, nCopies, nMoves, nUpdates, nCUpdates, nMUpdates, nSplits, nMerges


def edit_random_file(generate_path):
    languages = ["gh-python", "gh-java", "xml"]
    java_projs = ["apache-commons-cli", "drool", "elastic-search", "google-guava", "h2", "jabref", "killbill", "ok-http", "signal-server"]
    python_projs = ["ansible","black","django","home-assitant","keras","pyxel","requests","scikit-learn","textual","wagtail"]
    xml_projs = ["arthas","flutter","magisk","mall","okhttp","protobuf","sa-token","spring-cloud-alibaba","syncthing","tensorflow"]
    for r_seed in range(0, 3000):
        random.seed(r_seed)
        language = random.choice(languages)
        proj = random.choice(java_projs) if language == "gh-java" else random.choice(python_projs) if language == "gh-python" else random.choice(xml_projs)
        commit_sha = random.choice(os.listdir("D:\\git-repos\\ArchiDiff\\Analysis\\experiment_data\\" + language + "\\before\\" + proj))
        try:
            file = random.choice(os.listdir("D:\\git-repos\\ArchiDiff\\Analysis\\experiment_data\\" + language + "\\before\\" + proj + "\\" + commit_sha))
        except:
            print(r_seed, language, proj, commit_sha)
            continue
        file_extension = ".py" if language == "gh-python" else ".java" if language == "gh-java" else ".xml"
        left_file_path = generate_path + "\\" + str(r_seed) + "-left" + file_extension
        right_file_path = generate_path + "\\" + str(r_seed) + "-right" + file_extension
        with open("D:\\git-repos\\ArchiDiff\\Analysis\\experiment_data\\" + language + "\\before\\" + proj + "\\" + commit_sha + "\\" + file, "r", encoding="utf-8") as leftinfile:
            filelength_before = len(leftinfile.readlines())
            if filelength_before <= 1:
                continue
            nedits = random.randint(1, filelength_before)
        generated_ES = generate_edit_script("experiment_data\\" + language + "\\before\\" + proj + "\\" + commit_sha + "\\" + file, nedits, left_file_path,
                             right_file_path)
        right_file = open(right_file_path, "r", encoding='utf-8')
        filelength_after = len(right_file.readlines())
        right_file.close()
        if not generated_ES:
            print(r_seed, None)
            continue
        nEdits_machine, nDeletes_machine, nInserts_machine, nCopies_machine, nMoves_machine, nUpdates_machine, nCUpdates_machine, nMUpdates_machine, nSplits_machine, nMerges_machine = cal_indicators(generated_ES)
        BDiff_ES = BDiff.BDiffFile_evalulate(left_file_path, right_file_path)
        nEdits_bdiff, nDeletes_bdiff, nInserts_bdiff, nCopies_bdiff, nMoves_bdiff, nUpdates_bdiff, nCUpdates_bdiff, nMUpdates_bdiff, nSplits_bdiff, nMerges_bdiff = cal_indicators(BDiff_ES)
        right_ratio = cal_identical_percentage(unified_EA(generated_ES), unified_EA(BDiff_ES))
        sql = '''Insert into random_machine_evaluation (random_seed, Language, ProjName, CommitSha, FileName, FileLengthBefore, FileLengthAfter, right_ratio, nEdits_machine, EditScript_machine, nDeletes_machine, nInserts_machine, nCopies_machine, nMoves_machine, nUpdates_machine, nCUpdates_machine, nMUpdates_machine, nSplits_machine, nMerges_machine, nEdits_bdiff, EditScript_bdiff, nDeletes_bdiff, nInserts_bdiff, nCopies_bdiff, nMoves_bdiff, nUpdates_bdiff, nCUpdates_bdiff, nMUpdates_bdiff, nSplits_bdiff, nMerges_bdiff) \
                                           values (%d, '%s', '%s', '%s', '%s', %d, %d, '%s', %d, "%s", %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, "%s", %d, %d, %d, %d, %d, %d, %d, %d, %d)''' \
              % (r_seed, language, proj, commit_sha, file, filelength_before, filelength_after, right_ratio, nEdits_machine, str(generated_ES), nDeletes_machine, nInserts_machine, nCopies_machine, nMoves_machine, nUpdates_machine, nCUpdates_machine, nMUpdates_machine, nSplits_machine, nMerges_machine, \
                 nEdits_bdiff, str(BDiff_ES), nDeletes_bdiff, nInserts_bdiff, nCopies_bdiff, nMoves_bdiff, nUpdates_bdiff, nCUpdates_bdiff, nMUpdates_bdiff, nSplits_bdiff, nMerges_bdiff)
        cursor.execute(sql)
        conn.commit()
        print(r_seed, language, proj, commit_sha, file, right_ratio)

def update_commit_message(base_path, lang):
    cursor.execute("select distinct(CommitSha), ProjName from bdiffeditscripts where Language = '%s' and CommitMessage is NULL " % lang)
    commits = cursor.fetchall()
    for commit in commits:
        proj_path = base_path + "\\" + lang + "\\" + commit[1]
        commit_result = subprocess.run("git log -1 --pretty=format:'%B' " + commit[0], shell=True,
                                        stdout=subprocess.PIPE,
                                        text=True, cwd=proj_path, encoding="utf8")
        commit_message = commit_result.stdout
        print(commit[1], commit[0])
        sql1 = "update bdiffeditscripts set CommitMessage = %s where ProjName = %s and CommitSha = %s;"
        data1 = (conn.escape_string(commit_message), commit[1], commit[0])
        cursor.execute(sql1, data1)
        conn.commit()

def get_commit_message(base_path, lang, Proj, sha):
    proj_path = base_path + "\\" + lang + "\\" + Proj
    commit_result = subprocess.run("git log -1 --pretty=format:'%B' " + sha, shell=True,
                                   stdout=subprocess.PIPE,
                                   text=True, cwd=proj_path, encoding="utf8")
    return commit_result.stdout.encode('utf-8')