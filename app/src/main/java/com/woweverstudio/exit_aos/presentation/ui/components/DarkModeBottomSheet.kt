package com.woweverstudio.exit_aos.presentation.ui.components

import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitColors

/**
 * 다크 모드에서 시스템 네비게이션 바를 어둡게 유지하는 ModalBottomSheet
 * 
 * ModalBottomSheet가 열릴 때 시스템 네비게이션 바가 밝아지는 문제를 해결합니다.
 * ModalBottomSheet는 Dialog window를 사용하므로, Dialog의 window에서 네비게이션 바 색상을 변경합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarkModeBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    containerColor: Color = ExitColors.Background,
    contentColor: Color = contentColorFor(containerColor),
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        scrimColor = scrimColor,
        dragHandle = dragHandle
    ) {
        // Dialog window의 네비게이션 바를 어둡게 설정
        SetDialogNavigationBarDark()
        
        content()
    }
}

/**
 * Dialog window의 시스템 네비게이션 바를 다크 모드로 설정합니다.
 * ModalBottomSheet의 content 내부에서 호출해야 합니다.
 */
@Composable
private fun SetDialogNavigationBarDark() {
    val view = LocalView.current
    
    SideEffect {
        // Dialog의 window 가져오기
        val dialogWindow = findDialogWindow(view)
        
        dialogWindow?.let { window ->
            // 네비게이션 바를 어두운 색으로 설정
            window.navigationBarColor = android.graphics.Color.parseColor("#0A0A0A")
            
            // 네비게이션 바 아이콘을 밝게 (다크 모드)
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightNavigationBars = false
            }
            
            // 추가: Android 12 이상에서 navigation bar contrast 강제 비활성화
            window.isNavigationBarContrastEnforced = false
        }
    }
}

/**
 * View 계층을 탐색하여 Dialog의 Window를 찾습니다.
 */
private fun findDialogWindow(view: View): Window? {
    var currentView: View? = view
    while (currentView != null) {
        if (currentView.parent is DialogWindowProvider) {
            return (currentView.parent as DialogWindowProvider).window
        }
        currentView = currentView.parent as? View
    }
    return null
}
