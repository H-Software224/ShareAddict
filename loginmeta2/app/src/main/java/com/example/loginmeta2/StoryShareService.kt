package com.example.loginmeta2

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class StoryShareService : AccessibilityService() {

    companion object {
        private const val TAG = "StoryShareService"
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        Log.d(TAG, "Event received: ${event.packageName}")

        val root = rootInActiveWindow ?: return

        // 전체 노드 로그 찍기 (디버깅용)
        logAllNodes(root)

        // "내 스토리" 노드 찾기
        val storyNode = findNodeByText(root, "내 스토리")

        if (storyNode != null) {
            Log.d(TAG, "'내 스토리' node FOUND! Performing click.")
            storyNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            Log.d(TAG, "Could NOT find '내 스토리' node.")
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service interrupted")
    }

    // 🔍 노드 탐색 함수
    private fun findNodeByText(node: AccessibilityNodeInfo?, targetText: String): AccessibilityNodeInfo? {
        if (node == null) return null

        val text = node.text?.toString() ?: ""
        val desc = node.contentDescription?.toString() ?: ""

        if (text.contains(targetText) || desc.contains(targetText)) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val result = findNodeByText(child, targetText)
            if (result != null) return result
        }

        return null
    }

    // 🪵 디버깅용 전체 노드 트리 로그
    private fun logAllNodes(node: AccessibilityNodeInfo?, depth: Int = 0) {
        if (node == null) return

        val indent = "  ".repeat(depth)
        Log.d("NodeTree", "$indent• class: ${node.className}, text: ${node.text}, desc: ${node.contentDescription}, id: ${node.viewIdResourceName}")

        for (i in 0 until node.childCount) {
            logAllNodes(node.getChild(i), depth + 1)
        }
    }
}
