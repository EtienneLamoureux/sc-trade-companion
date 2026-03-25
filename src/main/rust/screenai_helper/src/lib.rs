//! Native helper for Chrome Screen AI model file callbacks.
//!
//! The Screen AI library requires file-reading callbacks registered via
//! SetFileContentFunctions(). These callbacks are invoked from native threads
//! with small stacks that cannot be attached to the JVM. This helper provides
//! pure native callback implementations that read model files from a configured
//! directory, avoiding the need for any JVM thread attachment.

use std::ffi::{c_char, c_uint, c_void, CStr};
use std::fs;
use std::sync::{LazyLock, Mutex};

static MODEL_DIR: LazyLock<Mutex<String>> = LazyLock::new(|| Mutex::new(String::new()));

/// Sets the model directory path. Must be called before the callbacks are used.
#[unsafe(no_mangle)]
pub extern "C" fn set_model_dir(dir: *const c_char) {
    let dir = unsafe { CStr::from_ptr(dir) }.to_string_lossy().to_string();
    *MODEL_DIR.lock().unwrap() = dir;
}

/// Returns the size in bytes of the model file at the given relative path.
#[unsafe(no_mangle)]
pub extern "C" fn get_file_content_size(relative_path: *const c_char) -> c_uint {
    let path = unsafe { CStr::from_ptr(relative_path) }.to_string_lossy();
    let full_path = format!("{}/{}", MODEL_DIR.lock().unwrap(), path);
    fs::metadata(&full_path)
        .map(|m| m.len() as c_uint)
        .unwrap_or(0)
}

/// Reads the contents of the model file into the provided buffer.
#[unsafe(no_mangle)]
pub extern "C" fn get_file_content(relative_path: *const c_char, size: c_uint, buffer: *mut c_void) {
    let path = unsafe { CStr::from_ptr(relative_path) }.to_string_lossy();
    let full_path = format!("{}/{}", MODEL_DIR.lock().unwrap(), path);
    if let Ok(data) = fs::read(&full_path) {
        let len = std::cmp::min(data.len(), size as usize);
        unsafe {
            std::ptr::copy_nonoverlapping(data.as_ptr(), buffer as *mut u8, len);
        }
    }
}
