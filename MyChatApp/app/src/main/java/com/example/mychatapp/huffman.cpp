#include<bits/stdc++.h>
#include<fstream>
#include "heap.h"
using namespace std;
bool checkequal(ifstream& in1, ifstream& in2)
{
    ifstream::pos_type size1, size2;

    size1 = in1.seekg(0, ifstream::end).tellg();
    in1.seekg(0, ifstream::beg);

    size2 = in2.seekg(0, ifstream::end).tellg();
    in2.seekg(0, ifstream::beg);

    if(size1 != size2)
        return false;

    while(!in1.eof() && !in2.eof())
    {
        char ch1=in1.get();
        char ch2=in2.get();
        if(ch1!=ch2)
            break;
    }
    if(!in1.eof())
        return false;
    return true;
}
signed main()
{
    ifstream fin;
    ofstream fout;
    string s,temp="";
    int ch[257]={0};
    fin.open("random.txt");
    while(!fin.eof())
    {
        char c;
        c=fin.get();
        ch[c]++;
    }
    fin.close();
    node* heap=new node[100000000];
    int siz=0;
    for(int i=0;i<256;i++)
    {
        if(ch[i]>0)
        {
            siz++;
            heap[siz].ch=i;
            heap[siz].freq=ch[i];
            heap[siz].left=new node;
            heap[siz].right=new node;
            heap[siz].left->ch=-1;
            heap[siz].right->ch=-1;
            heap[siz].left->freq=-1;
            heap[siz].right->freq=-1;
        }
    }
    build_heap(heap,siz);
    while(siz>1)
    {
        node* x1=new node;
        node* x2=new node;
        node* x3;
        x1=extract_min(heap,siz);
        x2=extract_min(heap,siz);
        x3=new node;
        x3->ch=256;
        x3->freq=x1->freq+x2->freq;
        x3->left=new node;
        x3->right=new node;
        x3->left=(x1->freq<x2->freq)?(x1):(x2);
        x3->right=(x1->freq>=x2->freq)?(x1):(x2);
        insert_key(heap,x3,siz);
    }
    traverse(&heap[1],"");
    map <string,char> ma;
    for(int i=0;i<=256;i++)
    {
        if(sol[i].size()>0)
        {
            cout<<(char)i<<": "<<sol[i]<<endl;
            ma[sol[i]]=i;
        }
        else
        ma[sol[i]]=NULL;
    }
    fin.open("random.txt");
    fout.open("compress.txt",ios_base::out);
    while(!fin.eof())
    {
        int c;
        c=fin.get();
        if(c!=-1)
        {
            for(int i=0;i<sol[c].size();i++)
            {
                temp+=sol[c][i];
                if(temp.size()==8)
                {
                    char tem[9];
                    for(int j=0;j<8;j++)
                        tem[j]=temp[j];
                    tem[8]=NULL;
                    char ch=strtol(tem,NULL,2);
                    temp.clear();
                    fout<<ch;
                }
            }
        }
    }
    string last=temp;
    temp.clear();
    fout.close();
    fin.close();

    // decompression

    fout.open("decompress.txt",ios_base::out);
    fin.open("compress.txt");
    string fi="";
    while(!fin.eof())
    {
        int ch=fin.get(),i;
        if(ch!=-1)
        {
            temp+=bitset<8>(ch).to_string();
            int sz=temp.size();
            while(sz>0)
            {
                fi+=temp[0];
                temp.erase(temp.begin());
                sz--;
                if(ma[fi]!=NULL)
                {
                    fout<<ma[fi];
                    fi.clear();
                }
            }
        }
    }
    string en=fi+last,st="";
    for(int i=0;i<en.size();i++)
    {
        st+=en[i];
        if(ma[st]!=NULL)
        {
          fout<<ma[st];
          st.clear();
        }
    }
    fin.close();
    fout.close();
    ifstream fin1,fin2;
    fin1.open("random.txt");
    fin2.open("decompress.txt");
    if(checkequal(fin1, fin2)) {
            cout << "Files are equal" << endl;
            exit(0);
        }
        else
        {
            cout << "Files are not equal" << endl;
            exit(1);
        }
    return 0;
}