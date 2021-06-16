//-------------------------------------------------------------------
// In Game Memory Profiler
// by Ricky Lung Man Tat (mtlung@gmail.com)


#ifndef __PROFILER_CALLSTACKPROFILER__
#define __PROFILER_CALLSTACKPROFILER__

#include "NonCopyable.h"

namespace Profiler {

/*!	A custom call stack class.
	Call stack is mostly usefull for debugging purpose, but getting the
	native call stack is very platform dependent and not reliable.
	Therfore this clas is made to act as a custom call stack information
 */
class PROFILER_API CallstackNode : Noncopyable
{
public:
	CallstackNode(sal_in_z const char name[], sal_maybenull CallstackNode* parent=nullptr);

	virtual ~CallstackNode();

// Operations
	/*!	Search for a node with the given name.
		A node will be returned if the name can be found, otherwise a new node
		will be created and returned.

		Searching steps:
		1) Search for direct children, if none then
		2) Search for all ancestor with non zero recursion count, if none then
		3) Create a new node

		\note The is the heart of the call stack construction process.
	 */
	sal_notnull CallstackNode* getChildByName(sal_in_z const char name[]);

	/*!	Call this to indicate the starting of a function.
		Derived class can overide this function to provide more action
		when a function begins, for instance to time how long the function
		call takes.
		\sa end()
	 */
	virtual void begin() {}

	/*!	Call this to indicate the ending of a function.
		\sa begin()
	 */
	virtual void end() {}

	/*!	To make CallstackNode behave polymorphically, derived class should override
		this method and return derived class's instance.
	 */
	virtual CallstackNode* createNode(sal_in_z const char name[], sal_maybenull CallstackNode* parent) = 0;

	/*!	Non-recursive version of pre-order traversal.
		Example:
		\code
		CallstackNode* n = rootNode;
		while((n = CallstackNode::traverse(n)) != nullptr) {
			// Do something with n...
		}
		\endcode
	 */
	static sal_maybenull CallstackNode* traverse(sal_maybenull CallstackNode* n);

// Attributes
	/*!	It stores a pointer to the node's node.
		The string that this pointer pointed to is suppose to
		be a statically allocated string, and we simply use this
		pointer for comparision rather than the content of the string.
	 */
	const char* const name;

	//! Get the call stack depth of this node
	size_t callDepth() const;

	// Pointers that make up the callstack tree structure
	CallstackNode* parent;
	CallstackNode* firstChild;
	CallstackNode* sibling;

	size_t recursionCount;
};	// CallstackNode

/*!	This class make use of the CallstackNode to create a
	tree like structure that represent a call stack.
	All you have to do is the call begin("function name") and
	end() inside the function you want to profile.

	This class is also supposed to be inherited to provide some
	concret profiling service.
 */
class PROFILER_API CallstackProfiler : Noncopyable
{
public:
	CallstackProfiler();

	virtual ~CallstackProfiler();

// Operations
	virtual void begin(sal_in_z const char name[]);

	virtual void end();

// Attributes
	/*!	Before the profiler can use, user should create and add
		an instance of concret CallstackNode. The life time of this
		root node will be handled by CallstackProfiler.
	 */
	void setRootNode(sal_maybenull CallstackNode* root);

	sal_maybenull CallstackNode* getRootNode() {
		return mRootNode;
	}

	sal_maybenull CallstackNode* getCurrentNode() {
		return mCurrentNode;
	}

	/*!	Run-time configurable flag to enable or disable the profiler.
		\note Do not change this value in between the call of begin() and end().
	 */
	bool enable;

protected:
	CallstackNode* mRootNode;
	CallstackNode* mCurrentNode;
};	// CallstackProfiler

}	// namespace Profiler

#endif	// __PROFILER_CALLSTACKPROFILER__
