#include<bits/stdc++.h>
using namespace std;
string sol[257];
struct node
{
    int ch;
    int freq;
    node* left;
    node* right;
};
void traverse(node* root,string s)
{
    if(root->left->ch==-1 && root->right->ch==-1)
    {
        sol[root->ch]=s;
        return;
    }
    if(root->left->ch>=0)
    traverse(root->left,s+'1');
    if(root->right->ch>=0)
    traverse(root->right,s+'0');
}
void min_heapify(node a[],int parent,int siz)
{
    int left=2*parent,right=2*parent+1;
    int largest=parent;
    if(left<=siz && a[left].freq<a[largest].freq)
    largest=left;
    if(right<=siz && a[right].freq<a[largest].freq)
    largest=right;
    if(largest!=parent)
    {
        swap(a[parent],a[largest]);
        min_heapify(a,largest,siz);
    }
}
void build_heap(node a[],int siz)
{
    int i;
    for(i=siz/2;i>=1;i--)
    min_heapify(a,i,siz);
}
node* extract_min(node a[],int &siz)
{
    node *ma=new node;
    ma->ch=a[1].ch;
    ma->freq=a[1].freq;
    ma->left=a[1].left;
    ma->right=a[1].right;
    swap(a[1],a[siz]);
    siz-=1;
    min_heapify(a,1,siz);
    return ma;
}
void increase_key(node a[],int index)
{
    while(index>1 && a[index/2].freq>a[index].freq)
    {
        swap(a[index/2],a[index]);
        index=index/2;
    }
}
void insert_key(node a[],node* value,int &siz)
{
    siz+=1;
    a[siz].ch=value->ch;
    a[siz].freq=value->freq;
    a[siz].left=new node;
    a[siz].right=new node;
    a[siz].left=value->left;
    a[siz].right=value->right;
    increase_key(a,siz);
}