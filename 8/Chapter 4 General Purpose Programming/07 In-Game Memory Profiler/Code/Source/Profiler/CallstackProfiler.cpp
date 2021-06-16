//-------------------------------------------------------------------
// In Game Memory Profiler
// by Ricky Lung Man Tat (mtlung@gmail.com)


#include "CallstackProfiler.h"
#include <assert.h>

namespace Profiler {

CallstackNode::CallstackNode(const char name[], CallstackNode* parent)
	:
	name(name),
	parent(parent), firstChild(nullptr), sibling(nullptr),
	recursionCount(0)
{
}

CallstackNode::~CallstackNode()
{
	delete firstChild;
	delete sibling;
}

CallstackNode* CallstackNode::getChildByName(const char name_[])
{
	{	// Try to find a node with the supplied name
		CallstackNode* n = firstChild;
		while(n) {
			// NOTE: We are comparing the string's pointer directly
			if(n->name == name_)
				return n;
			n = n->sibling;
		}
	}

	{	// Search for ancestor with non zero recursion
		CallstackNode* n = parent;
		while(n) {
			if(n->name == name_ && n->recursionCount > 0)
				return n;
			n = n->parent;
		}
	}

	{	// We didn't find it, so create a new one
		CallstackNode* node = createNode(name_, this);

		if(firstChild) {
			CallstackNode* lastChild;
			CallstackNode* tmp = firstChild;
			do {
				lastChild = tmp;
				tmp = tmp->sibling;
			} while(tmp);
			lastChild->sibling = node;
		} else {
			firstChild = node;
		}

		return node;
	}
}

CallstackNode* CallstackNode::traverse(CallstackNode* n)
{
	if(!n) return nullptr;

	if(n->firstChild)
		n = n->firstChild;
	else {
		while(!n->sibling) {
			n = n->parent;
			if(!n)
				return nullptr;
		}
		n = n->sibling;
	}
	return n;
}

size_t CallstackNode::callDepth() const
{
	CallstackNode* p = parent;
	size_t depth = 0;
	while(p) {
		p = p->parent;
		++depth;
	}
	return depth;
}

CallstackProfiler::CallstackProfiler()
	: enable(false), mRootNode(nullptr), mCurrentNode(nullptr)
{}

CallstackProfiler::~CallstackProfiler() {
	setRootNode(nullptr);
}

void CallstackProfiler::setRootNode(CallstackNode* root)
{
	delete mRootNode;
	mCurrentNode = mRootNode = root;
}

void CallstackProfiler::begin(const char name[])
{
	if(!enable)
		return;

	assert(mCurrentNode);

	CallstackNode* node = mCurrentNode;
	if(name != node->name) {
		node = node->getChildByName(name);

		// Only alter the current node, if the child node is not recursing
		if(node->recursionCount == 0)
			mCurrentNode = node;
	}
	node->begin();
	node->recursionCount++;
}

void CallstackProfiler::end()
{
	if(!enable)
		return;

	assert(mCurrentNode);
	mCurrentNode->recursionCount--;
	mCurrentNode->end();

	// Only back to the parent when the current node is not inside a recursive function
	if(mCurrentNode->recursionCount == 0)
		mCurrentNode = mCurrentNode->parent;
}

}	// namespace Profiler
